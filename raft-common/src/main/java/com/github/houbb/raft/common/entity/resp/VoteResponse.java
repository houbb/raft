package com.github.houbb.raft.common.entity.resp;

/**
 * 投票响应
 * @since 1.0.0
 */
public class VoteResponse extends BaseRaftResponse {

    /** 当前任期号，以便于候选人去更新自己的任期 */
    private long term;

    /** 候选人赢得了此张选票时为真 */
    private boolean voteGranted;

    public VoteResponse(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public VoteResponse() {
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public static VoteResponse fail() {
        VoteResponse response = new VoteResponse();
        response.setVoteGranted(false);
        return response;
    }

}
