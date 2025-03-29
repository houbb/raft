package com.github.houbb.raft.server.core.impl;

import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.ClientKeyValueRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.ClientKeyValueResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.server.core.Node;

public class DefaultNode implements Node {
    @Override
    public void setConfig(NodeConfig config) {

    }

    @Override
    public VoteResponse handlerRequestVote(VoteRequest param) {
        return null;
    }

    @Override
    public AppendLogResponse handlerAppendEntries(AppendLogRequest param) {
        return null;
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

    }

    @Override
    public void destroy() throws Throwable {

    }
}
