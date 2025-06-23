package com.github.houbb.raft.server.core;

import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;

/**
 *  一致性模块接口
 *
 * 1. leader 选举
 * 2. 日志复制。
 *
 * 实现这两个接口是 Raft 的关键所在。
 *
 * @since 1.0.0
 */
public interface Consensus {

    /**
     * 请求投票 RPC
     *
     * 接收者实现：
     *
     * 1. 如果term 小于 currentTerm返回 false （5.2 节）
     *
     * 2. 如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     *
     * @param request 请求
     * @return 结果
     */
    VoteResponse vote(VoteRequest request);

    /**
     * 附加日志(多个日志,为了提高效率) RPC
     *
     * 接收者实现：
     *
     *    如果 term 小于 currentTerm 就返回 false （5.1 节）
     *    如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false （5.3 节）
     *    如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的 （5.3 节）
     *    附加任何在已有的日志中不存在的条目
     *    如果 leaderCommit 大于 commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
     *
     * @param request 请求
     * @return 结果
     */
    AppendLogResponse appendLog(AppendLogRequest request);

}
