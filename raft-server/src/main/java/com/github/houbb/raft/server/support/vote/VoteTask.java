package com.github.houbb.raft.server.support.vote;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.RpcRequestCmdConst;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.common.rpc.RpcClient;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;
import com.github.houbb.raft.server.support.peer.PeerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 投定时调度
 *
 * 1. 在转变成候选人后就立即开始选举过程
 * 自增当前的任期号（currentTerm）
 * 给自己投票
 * 重置选举超时计时器
 * 发送请求投票的 RPC 给其他所有服务器
 * 2. 如果接收到大多数服务器的选票，那么就变成领导人
 * 3. 如果接收到来自新的领导人的附加日志 RPC，转变成跟随者
 * 4. 如果选举过程超时，再次发起一轮选举
 *
 * @since 1.0.0
 */
public class VoteTask implements Runnable {

    private final Log log = LogFactory.getLog(VoteTask.class);

    private final NodeInfoContext nodeInfoContext;

    public VoteTask(NodeInfoContext nodeInfoContext) {
        this.nodeInfoContext = nodeInfoContext;
    }

    @Override
    public void run() {
        //1. leader 不参与选举
        if(NodeStatusEnum.LEADER.equals(nodeInfoContext.getStatus())) {
            log.info("[Raft] current status is leader, ignore vote.");
            return;
        }

        //2. 判断两次的时间间隔
        boolean isFitElectionTime = isFitElectionTime();
        if(!isFitElectionTime) {
            return;
        }

        //3. 开始准备选举
        //3.1 状态候选
        nodeInfoContext.setStatus(NodeStatusEnum.CANDIDATE);
        log.info("Node will become CANDIDATE and start election leader, info={}", nodeInfoContext);
        //3.2 上一次的选票时间
        nodeInfoContext.setPreElectionTime(getPreElectionTime());
        //3.3 term 自增
        nodeInfoContext.setCurrentTerm(nodeInfoContext.getCurrentTerm()+1);
        //3.4 给自己投票
        final PeerManager peerManager = nodeInfoContext.getPeerManager();
        final String selfAddress = peerManager.getSelf().getAddress();
        nodeInfoContext.setVotedFor(selfAddress);
        //通知其他除了自己的节点（暂时使用同步，后续应该优化为异步线程池，这里为了简化流程）
        // TODO: 需要考虑超时的情况
        final List<PeerInfoDto> allPeerList = peerManager.getList();
        List<VoteResponse> voteResponseList = new ArrayList<>();
        for(PeerInfoDto remotePeer : allPeerList) {
            // 跳过自己
            if(remotePeer.getAddress().equals(selfAddress)) {
                continue;
            }

            // 远程投票
            try {
                VoteResponse response = voteSelfToRemote(remotePeer, selfAddress, nodeInfoContext);
                voteResponseList.add(response);
            } catch (Exception e) {
                log.error("voteSelfToRemote meet ex, remotePeer={}", remotePeer, e);
            }
        }

        //3.5 判断选举结果
        int voteSuccessTotal = calcVoteSuccessVote(voteResponseList, nodeInfoContext);
        // 如果投票期间,有其他服务器发送 appendEntry , 就可能变成 follower ,这时,应该停止.
        if (NodeStatusEnum.FOLLOWER.equals(nodeInfoContext.getStatus())) {
            log.info("[Raft] 如果投票期间,有其他服务器发送 appendEntry, 就可能变成 follower, 这时,应该停止.");
            return;
        }

        // 是否超过一半？加上自己，等于也行。自己此时没算
        if(voteSuccessTotal >= peerManager.getList().size() / 2) {
            log.warn("[Raft] leader node vote success become leader {}", selfAddress);
            nodeInfoContext.setStatus(NodeStatusEnum.LEADER);
            peerManager.setLeader(peerManager.getSelf());
            // 投票人信息清空
            nodeInfoContext.setVotedFor("");
            // 成主之后做的一些事情
            afterBeingLeader(nodeInfoContext);
        } else {
            // 投票人信息清空 重新选举
            nodeInfoContext.setVotedFor("");
            log.warn("vote failed, wait next vote");
        }

        // 再次更新选举时间 为什么？？？？
        nodeInfoContext.setPreElectionTime(getPreElectionTime());
    }

    /**
     * 随机  获取上一次的选举时间
     * @return 时间
     */
    private long getPreElectionTime() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150;
    }


    /**
     * 初始化所有的 nextIndex 值为自己的最后一条日志的 index + 1. 如果下次 RPC 时, 跟随者和leader 不一致,就会失败.
     * 那么 leader 尝试递减 nextIndex 并进行重试.最终将达成一致.
     * @param nodeInfoContext 上下文
     */
    private void afterBeingLeader(NodeInfoContext nodeInfoContext) {
        //todo...  这个后续再日志复制部分实现
    }

    /**
     * 计算投票给自己的数量
     * 1. 同时需要更新自己的任期
     * @param voteResponseList 结果列表
     * @param nodeInfoContext 基本信息
     * @return 结果
     */
    private int calcVoteSuccessVote(List<VoteResponse> voteResponseList,
                                    final NodeInfoContext nodeInfoContext) {
        int sum = 0;

        for(VoteResponse response : voteResponseList) {
            if(response == null) {
                log.error("response is null");
                continue;
            }

            // 投票给自己
            boolean isVoteGranted = response.isVoteGranted();
            if (isVoteGranted) {
                sum++;
            } else {
                // 更新自己的任期.
                long resTerm = response.getTerm();
                if (resTerm >= nodeInfoContext.getCurrentTerm()) {
                    nodeInfoContext.setCurrentTerm(resTerm);
                    log.info("[Raft] update current term from vote res={}", response);
                }
            }
        }

        log.info("calcVoteSuccessVote sum={}", sum);
        return sum;
    }

    private VoteResponse voteSelfToRemote(PeerInfoDto remotePeer,
                                          final String selfAddress,
                                          final NodeInfoContext nodeInfoContext) {
        final LogManager logManager = nodeInfoContext.getLogManager();
        // 当前最后的 term
        long lastTerm = 0L;
        LogEntry last = logManager.getLast();
        if (last != null) {
            lastTerm = last.getTerm();
        }

        VoteRequest param = new VoteRequest();
        param.setTerm(nodeInfoContext.getCurrentTerm());
        param.setCandidateId(nodeInfoContext.getPeerManager().getSelf().getAddress());
        long logIndex = logManager.getLastIndex() == null ? 0 : logManager.getLastIndex();
        param.setLastLogIndex(logIndex);
        param.setLastLogTerm(lastTerm);

        RpcRequest request = new RpcRequest();
        request.setCmd(RpcRequestCmdConst.R_VOTE);
        request.setObj(param);
        request.setUrl(remotePeer.getAddress());

        // 发送
        final RpcClient rpcClient = nodeInfoContext.getRpcClient();
        // 请求超时时间，后续可以考虑配置化
        VoteResponse voteResponse = rpcClient.send(request, 30);
        return voteResponse;
    }

    /**
     * 是否满足选举的时间
     *
     * @return 结果
     */
    private boolean isFitElectionTime() {
        long electionTime = nodeInfoContext.getElectionTime();
        long preElectionTime = nodeInfoContext.getPreElectionTime();

        //基于 RAFT 的随机时间,解决冲突.
        // 这里不会导致这个值越来越大吗？？？
        long randomElectionTime = electionTime + ThreadLocalRandom.current().nextInt(50);
        nodeInfoContext.setElectionTime(randomElectionTime);

        long current = System.currentTimeMillis();
        if (current - preElectionTime < randomElectionTime) {
            log.warn("[Raft] current electionTime is not fit, ignore handle");

            return false;
        }
        return true;
    }

}
