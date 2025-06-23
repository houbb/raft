package com.github.houbb.raft.server.bs;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.server.core.Node;
import com.github.houbb.raft.server.core.impl.DefaultNode;

import java.util.List;

/**
 * raft 启动引导类
 *
 * @since 1.0.0
 */
public class RaftBootstrap {

    private static final Log log = LogFactory.getLog(RaftBootstrap.class);

    /**
     * 当前服务节点
     */
    private final int serverPort;

    /**
     * 集群启动列表
     */
    private final List<String> clusterAddressList;

    /**
     * 默认节点
     */
    private Node node = new DefaultNode();

    public RaftBootstrap(int serverPort,
                         List<String> clusterAddressList) {
        ArgUtil.notEmpty(clusterAddressList, "clusterAddressList");
        ArgUtil.notNegative(serverPort, "serverPort");

        this.serverPort = serverPort;
        this.clusterAddressList = clusterAddressList;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void boot() throws Throwable {
        // 配置信息
        NodeConfig config = new NodeConfig();
        // 自身节点
        config.setSelfPort(serverPort);
        // 其他节点地址
        config.setPeerAddressList(clusterAddressList);
        node.setConfig(config);
        log.info("[Rate] config={}", JSON.toJSONString(config));
        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (node) {
                node.notifyAll();
            }
        }));

        log.info("[Raft] RaftBootstrap gracefully wait");

        synchronized (node) {
            node.wait();
        }

        log.info("[Raft] RaftBootstrap gracefully stop");
        node.destroy();
    }
}
