package com.github.houbb.raft.common.entity.resp;

/**
 * 附加日志响应
 *
 * @since 1.0.0
 */
public class AppendLogResponse extends BaseRaftResponse {

    /** 当前的任期号，用于领导人去更新自己 */
    private long term;

    /** 跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真  */
    private boolean success;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
