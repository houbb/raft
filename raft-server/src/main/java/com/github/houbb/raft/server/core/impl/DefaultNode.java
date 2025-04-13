package com.github.houbb.raft.server.core.impl;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.ClientKeyValueRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.ClientKeyValueResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.server.core.Consensus;
import com.github.houbb.raft.server.core.Node;

public class DefaultNode implements Node {

    private static final Log log = LogFactory.getLog(DefaultNode.class);

    /** 一致性模块实现 */
    private Consensus consensus;

    @Override
    public void setConfig(NodeConfig config) {

    }

    @Override
    public VoteResponse handlerRequestVote(VoteRequest param) {
        log.info("handlerRequestVote req={}", param);
        return consensus.vote(param);
    }

    @Override
    public AppendLogResponse handlerAppendEntries(AppendLogRequest param) {
        log.info("handlerAppendEntries req={}", param);
        return consensus.appendLog(param);
    }

    @Override
    public ClientKeyValueResponse handlerClientRequest(ClientKeyValueRequest request) {
        return null;
    }

    @Override
    public ClientKeyValueResponse redirect(ClientKeyValueRequest request) {
        return null;
    }

    @Override
    public void init() throws Throwable {
        //1. 固定初始化开始选举调度
    }

    @Override
    public void destroy() throws Throwable {

    }
}
