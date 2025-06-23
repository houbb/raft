package com.github.houbb.raft.server.support.replication;

import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;

import java.util.concurrent.Future;

public interface IRaftReplication {

    /**
     * 备份
     * @param nodeInfoContext 上下文
     * @param peer 节点
     * @param entry 日志
     */
    Future<Boolean> replication(final NodeInfoContext nodeInfoContext, PeerInfoDto peer, LogEntry entry);


}
