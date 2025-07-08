package com.github.houbb.raft.server.support.peer;

import com.github.houbb.raft.server.dto.PeerInfoDto;

/**
 * 分布式节点管理
 *
 * @since 1.1.0
 */
public interface IClusterPeerManager {

    /**
     * 添加
     * @param peerInfoDto 临时对象
     * @return 结果
     */
    ClusterPeerResult addPeer(final PeerInfoDto peerInfoDto);

    /**
     * 移除
     * @param peerInfoDto 临时对象
     * @return 结果
     */
    ClusterPeerResult removePeer(final PeerInfoDto peerInfoDto);

}
