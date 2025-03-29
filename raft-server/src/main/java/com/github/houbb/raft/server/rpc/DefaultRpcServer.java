package com.github.houbb.raft.server.rpc;

import com.alipay.remoting.BizContext;
import com.github.houbb.raft.common.constant.RpcRequestCmdConst;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.ClientKeyValueRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.common.rpc.RpcResponse;
import com.github.houbb.raft.server.core.Node;

/**
 * 默认实现
 */
public class DefaultRpcServer implements RpcServer {

    /**
     * 节点信息
     */
    private final Node node;

    /**
     * 服务启动的端口
     */
    private final int port;

    /**
     * rpc 服务实现
     */
    private com.alipay.remoting.rpc.RpcServer rpcServer;

    public DefaultRpcServer(Node node, int port) {
        this.node = node;
        this.port = port;
    }

    @Override
    public RpcResponse<?> handlerRequest(RpcRequest request) {
        // 根据客户端的请求，做具体的实现的路由

        final int cmd = request.getCmd();
        final Object reqObj = request.getObj();

        if (cmd == RpcRequestCmdConst.R_VOTE) {
            return new RpcResponse<>(node.handlerRequestVote((VoteRequest) reqObj));
        } else if (request.getCmd() == RpcRequestCmdConst.A_ENTRIES) {
            return new RpcResponse<>(node.handlerAppendEntries((AppendLogRequest) reqObj));
        } else if (request.getCmd() == RpcRequestCmdConst.CLIENT_REQ) {
            return new RpcResponse<>(node.handlerClientRequest((ClientKeyValueRequest) reqObj));
        }

        // TODO 节点变更，暂时不处理。后续处理

        return null;
    }

    @Override
    public void init() throws Throwable {
        rpcServer = new com.alipay.remoting.rpc.RpcServer(port, false, false);
        rpcServer.registerUserProcessor(new RaftUserProcessor<RpcRequest>() {
            @Override
            public Object handleRequest(BizContext bizContext, RpcRequest rpcRequest) throws Exception {
                return handlerRequest(rpcRequest);
            }
        });

        // 服务启动
        rpcServer.startup();
    }

    @Override
    public void destroy() throws Throwable {
        rpcServer.shutdown();
    }

}
