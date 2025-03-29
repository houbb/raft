package com.github.houbb.raft.common.entity.req;

import com.github.houbb.raft.common.entity.req.dto.LogEntry;

/**
 * 附加日志请求
 * @since 1.0.0
 */
public class AppendLogRequest extends BaseRaftRequest{

    /** 候选人的任期号  */
    private long term;

    /** 被请求者 ID(ip:selfPort) */
    private String serverId;

    /** 领导人的 Id，以便于跟随者重定向请求 */
    private String leaderId;

    /**新的日志条目紧随之前的索引值  */
    private long prevLogIndex;

    /** prevLogIndex 条目的任期号  */
    private long preLogTerm;

    /** 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率） */
    private LogEntry[] entries;

    /** 领导人已经提交的日志的索引值  */
    private long leaderCommit;

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

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public LogEntry[] getEntries() {
        return entries;
    }

    public void setEntries(LogEntry[] entries) {
        this.entries = entries;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}
