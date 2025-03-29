package com.github.houbb.raft.common.entity.req;

/**
 * 投票请求
 *
 * @since 1.0.0
 */
public class VoteRequest extends BaseRaftRequest {

    /** 候选人的任期号  */
    private long term;

    /** 被请求者 ID(ip:selfPort) */
    private String serverId;

    /** 请求选票的候选人的 Id(ip:selfPort) */
    private String candidateId;

    /** 候选人的最后日志条目的索引值 */
    private long lastLogIndex;

    /** 候选人最后日志条目的任期号  */
    private long lastLogTerm;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }
}
