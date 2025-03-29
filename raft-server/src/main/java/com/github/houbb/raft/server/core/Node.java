package com.github.houbb.raft.server.core;

import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.ClientKeyValueRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.ClientKeyValueResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;

/**
 * 节点
 *
 * 首先，一个 Node 肯定需要配置文件，所以有一个 setConfig 接口，
 *
 * 然后，肯定需要处理“请求投票”和“附加日志”，
 *
 * 同时，还需要接收用户，也就是客户端的请求（不然数据从哪来？），
 *
 * 所以有 handlerClientRequest 接口，最后，考虑到灵活性，
 *
 * 我们让每个节点都可以接收客户端的请求，但 follower 节点并不能处理请求，所以需要重定向到 leader 节点，因此，我们需要一个重定向接口。
 *
 */
public interface Node {

    /**
     * 设置配置文件.
     *
     * @param config 配置
     */
    void setConfig(NodeConfig config);

    /**
     * 处理请求投票 RPC.
     *
     * @param param 请求
     * @return 结果
     */
    VoteResponse handlerRequestVote(VoteRequest param);

    /**
     * 处理附加日志请求.
     *
     * @param param 请求
     * @return v结果
     */
    AppendLogResponse handlerAppendEntries(AppendLogRequest param);

    /**
     * 处理客户端请求.
     *
     * @param request 请求
     * @return 结果
     */
    ClientKeyValueResponse handlerClientRequest(ClientKeyValueRequest request);

    /**
     * 转发给 leader 节点.
     * @param request 请求
     * @return 结果
     */
    ClientKeyValueResponse redirect(ClientKeyValueRequest request);

}
