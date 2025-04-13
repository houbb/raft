package com.github.houbb.raft.server.dto.node;

import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.rpc.RpcClient;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.support.peer.PeerManager;

/**
 * 节点信息管理类
 * @since 1.0.0
 */
public class NodeInfoContext {

    /**
     * 节点状态
     */
    private volatile NodeStatusEnum status = NodeStatusEnum.FOLLOWER;

    /** 选举时间间隔基数 */
    private volatile long electionTime = 15 * 1000;
    /** 上一次选举时间 */
    private volatile long preElectionTime = 0;

    /** 服务器最后一次知道的任期号（初始化为 0，持续递增） */
    private volatile long currentTerm = 0;

    /** 在当前获得选票的候选人的 Id */
    private volatile String votedFor;

    /**
     * 节点信息管理
     */
    private PeerManager peerManager;

    /** 日志条目集；每一个条目包含一个用户状态机执行的指令，和收到时的任期号 */
    private LogManager logManager;

    /**
     * rpc 客户端
     */
    private RpcClient rpcClient;

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void setPeerManager(PeerManager peerManager) {
        this.peerManager = peerManager;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public long getElectionTime() {
        return electionTime;
    }

    public void setElectionTime(long electionTime) {
        this.electionTime = electionTime;
    }

    public long getPreElectionTime() {
        return preElectionTime;
    }

    public void setPreElectionTime(long preElectionTime) {
        this.preElectionTime = preElectionTime;
    }

    public NodeStatusEnum getStatus() {
        return status;
    }

    public void setStatus(NodeStatusEnum status) {
        this.status = status;
    }
}
