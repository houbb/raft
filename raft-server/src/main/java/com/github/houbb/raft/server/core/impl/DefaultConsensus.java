package com.github.houbb.raft.server.core.impl;

import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.ArrayUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.server.core.Consensus;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.core.StateMachine;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;
import com.github.houbb.raft.server.support.peer.PeerManager;
import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 默认一致性实现
 * @since 1.0.0
 */
public class DefaultConsensus implements Consensus {

    private static final Log log = LogFactory.getLog(DefaultConsensus.class);

    /**
     * 选举锁
     */
    private final ReentrantLock voteLock = new ReentrantLock();

    /**
     * 附加日志锁
     */
    private final ReentrantLock appendLogLock = new ReentrantLock();

    /**
     * node 信息上下文
     */
    private final NodeInfoContext nodeInfoContext;

    public DefaultConsensus(NodeInfoContext nodeInfoContext) {
        this.nodeInfoContext = nodeInfoContext;
    }

    /**
     * 接收者实现：
     *      主要时先做一个抢占锁的动作，失败，则直接返回。
     *
     *      如果term < currentTerm返回 false （5.2 节）
     *      如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     *
     * @param request 请求
     * @return 结果
     */
    @Override
    public VoteResponse vote(VoteRequest request) {
        final long currentTerm = nodeInfoContext.getCurrentTerm();
        final String currentVoteFor = nodeInfoContext.getVotedFor();
        final PeerManager peerManager = nodeInfoContext.getPeerManager();

        final VoteResponse voteResponse = new VoteResponse();
        voteResponse.setTerm(currentTerm);
        voteResponse.setVoteGranted(false);

        final long reqTerm = request.getTerm();

        try {
            //1. 抢占锁
            boolean tryLogFlag = voteLock.tryLock();
            if(!tryLogFlag) {
                log.info("vote for request={} tryLock false", request);
                return voteResponse;
            }

            //2.1 如果term < currentTerm返回 false （5.2 节）
            if(reqTerm < currentTerm) {
                log.info("vote for reqTerm={} < currentTerm={}", reqTerm, currentTerm);
                return voteResponse;
            }

            log.info("node {} currentTerm={}. current vote for [{}], paramCandidateId : {}, paramTerm={}",
                    peerManager.getSelf(),
                    currentTerm,
                    currentVoteFor,
                    request.getCandidateId(),
                    request.getTerm()
                    );

            //2.2 (当前节点并没有投票 或者 已经投票过了且是对方节点) && 对方日志和自己一样新
            boolean isMatchVoteCondition = isMatchVoteCondition(request);
            if(!isMatchVoteCondition) {
                return voteResponse;
            }

            //2.3 如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
            final LogManager logManager = nodeInfoContext.getLogManager();
            // 对方没有自己新
            if (logManager.getLast().getTerm() > request.getLastLogTerm()) {
                log.info("request lastTerm is too old.");
                return voteResponse;
            }
            // 对方没有自己新
            if (logManager.getLastIndex() > request.getLastLogIndex()) {
                log.info("request lastIndex is too old.");
                return voteResponse;
            }

            //3. 满足
            nodeInfoContext.setStatus(NodeStatusEnum.FOLLOWER);
            nodeInfoContext.setCurrentTerm(reqTerm);
            nodeInfoContext.setVotedFor(request.getServerId()); //serverId 和 candidateId 是一样的，为什么要两个？
            peerManager.setLeader(new PeerInfoDto(request.getCandidateId()));

            //4. 返回成功
            voteResponse.setTerm(reqTerm);
            voteResponse.setVoteGranted(true);
            return voteResponse;
        } catch (Exception e) {
            log.error("Vote meet ex, req={}", request, e);
            return voteResponse;
        } finally {
            voteLock.unlock();
        }
    }

    /**
     * 满足投票条件
     * @param request 请求
     * @return 结果
     */
    private boolean isMatchVoteCondition(VoteRequest request) {
        final String currentVoteFor = nodeInfoContext.getVotedFor();
        if(StringUtil.isEmpty(currentVoteFor)
                || currentVoteFor.equals(request.getCandidateId())) {
            return true;
        }
        return false;
    }

    /**
     * 添加日志
     *
     * 接收者实现：
     *    如果 term < currentTerm 就返回 false （5.1 节）
     *    如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false （5.3 节）
     *    如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的 （5.3 节）
     *    附加任何在已有的日志中不存在的条目
     *    如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
     *
     * @param request 请求
     * @return 结果
     */
    @Override
    public AppendLogResponse appendLog(AppendLogRequest request) {
        AppendLogResponse appendLogResponse = new AppendLogResponse();
        final long currentTerm = nodeInfoContext.getCurrentTerm();
        appendLogResponse.setTerm(currentTerm);
        appendLogResponse.setSuccess(false);

        final long reqTerm = request.getTerm();
        try {
            //1.1 抢占锁
            boolean tryLockFlag = appendLogLock.tryLock();
            if(!tryLockFlag) {
                log.warn("[AppendLog] tryLog false");
                return appendLogResponse;
            }
            //1.2 是否够格？
            if(currentTerm > request.getTerm()) {
                log.warn("[AppendLog] currentTerm={} > reqTerm={}", currentTerm, reqTerm);
                return appendLogResponse;
            }

            //2.1 基本信息更新 为什么这样设置？
            final PeerManager peerManager = nodeInfoContext.getPeerManager();
            final long nowMills = System.currentTimeMillis();
            nodeInfoContext.setElectionTime(nowMills);
            nodeInfoContext.setPreElectionTime(nowMills);
            nodeInfoContext.setStatus(NodeStatusEnum.FOLLOWER);
            nodeInfoContext.setCurrentTerm(reqTerm);
            peerManager.setLeader(new PeerInfoDto(request.getLeaderId()));
            log.info("[AppendLog] update electionTime={}, status=Follower, term={}, leader={}",
                    nowMills, reqTerm, request.getLeaderId());

            //3.1 处理心跳
            if(ArrayUtil.isEmpty(request.getEntries())) {
                handleHeartbeat(request);

                //3.2 返回响应
                appendLogResponse.setTerm(reqTerm);
                appendLogResponse.setSuccess(true);
                return appendLogResponse;
            }

            return appendLogResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            appendLogLock.unlock();
        }
    }

    private void handleHeartbeat(AppendLogRequest request) {
        final long startTime = System.currentTimeMillis();
        log.info("handleHeartbeat start req={}", request);

        final LogManager logManager = nodeInfoContext.getLogManager();

        // 处理 leader 已提交但未应用到状态机的日志

        // 下一个需要提交的日志的索引（如有）
        long nextCommit = nodeInfoContext.getCommitIndex() + 1;

        //如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
        // 为什么？为了方便把缺失的日志，全部加上
        if (request.getLeaderCommit() > nodeInfoContext.getCommitIndex()) {
            int commitIndex = (int) Math.min(request.getLeaderCommit(), logManager.getLastIndex());
            nodeInfoContext.setCommitIndex(commitIndex);
            nodeInfoContext.setLastApplied(commitIndex);
        }

        final StateMachine stateMachine = nodeInfoContext.getStateMachine();
        while (nextCommit <= nodeInfoContext.getCommitIndex()){
            // 提交之前的日志
            // todo: 状态机需要基于 kv 实现
            stateMachine.apply(logManager.read(nextCommit));

            nextCommit++;
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("handleHeartbeat start end, costTime={}", costTime);
    }


}
