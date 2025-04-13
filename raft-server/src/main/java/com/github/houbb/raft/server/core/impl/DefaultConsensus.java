package com.github.houbb.raft.server.core.impl;

import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.server.core.Consensus;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;
import com.github.houbb.raft.server.support.peer.PeerManager;

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

    @Override
    public AppendLogResponse appendLog(AppendLogRequest request) {
        return null;
    }


}
