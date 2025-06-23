package com.github.houbb.raft.server.util;

import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.support.peer.PeerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0.0
 */
public class InnerPeerUtil {

    public static PeerManager initPeerManager(final NodeConfig config) {
        PeerManager peerManager = new PeerManager();

        List<PeerInfoDto> list = new ArrayList<>();
        for (String s : config.getPeerAddressList()) {
            PeerInfoDto peer = new PeerInfoDto(s);
            list.add(peer);

            // 暂时这里简单写死，后续应该调整优化
            if (s.equals("localhost:" + config.getSelfPort())) {
                peerManager.setSelf(peer);
            }
        }

        // 设置全部
        peerManager.setList(list);
        return peerManager;
    }

}
