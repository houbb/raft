package com.github.houbb.raft.common.entity.req;

/**
 * 投票请求
 *
 * @since 1.0.0
 */
public class VoteRequest extends BaseRaftRequest {

    /**
     * 为谁投票
     *
     * PS: 发现其他线程如果设置为空，再另一个地方取不到值
     * 可能是我的写法问题
     *
     * @since 1.0.0
     */
    private String votedFor;

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

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

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
