package com.github.houbb.raft.server.support.peer;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.RpcRequestCmdConst;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;

/**
 * 默认实现
 *
 * @since 1.1.0
 */
public class ClusterPeerManager implements IClusterPeerManager {

    private final Log log = LogFactory.getLog(ClusterPeerManager.class);

    private final NodeInfoContext node;

    public ClusterPeerManager(NodeInfoContext node) {
        ArgUtil.notNull(node, "node");

        this.node = node;
    }

    @Override
    public synchronized ClusterPeerResult addPeer(PeerInfoDto newPeer) {
        // 已经存在
        if (node.getPeerManager().getPeersWithOutSelf().contains(newPeer)) {
            log.warn("add peer exists={}", newPeer);

            return new ClusterPeerResult();
        }

        // 如果当前不是 leader 怎么办？
        node.getPeerManager().getPeersWithOutSelf().add(newPeer);

        if (node.getStatus() == NodeStatusEnum.LEADER) {
            node.getNextIndexes().put(newPeer, 0L);
            node.getMatchIndexes().put(newPeer, 0L);

            for (long i = 0; i < node.getLogManager().getLastIndex(); i++) {
                LogEntry entry = node.getLogManager().read(i);
                if (entry != null) {
                    // 备份
                    node.getRaftReplication().replication(node, newPeer, entry);
                }
            }

            for (PeerInfoDto ignore : node.getPeerManager().getPeersWithOutSelf()) {
                // TODO 同步到其他节点.
                RpcRequest request = new RpcRequest();
                request.setCmd(RpcRequestCmdConst.CHANGE_CONFIG_ADD);
                request.setUrl(newPeer.getAddress());
                request.setObj(newPeer);

                ClusterPeerResult result = node.getRpcClient().send(request);
                if (result != null && result.isSuccess()) {
                    log.info("replication config success, peer : {}, newServer : {}", newPeer, newPeer);
                } else {
                    // 失败了会怎么样？
                    log.warn("replication config fail, peer : {}, newServer : {}", newPeer, newPeer);
                }
            }
        }

        return new ClusterPeerResult();
    }

    @Override
    public synchronized ClusterPeerResult removePeer(PeerInfoDto oldPeer) {
        node.getPeerManager().getPeersWithOutSelf().remove(oldPeer);
        node.getNextIndexes().remove(oldPeer);
        node.getMatchIndexes().remove(oldPeer);

        return new ClusterPeerResult();
    }
}
