package com.github.houbb.raft.server.rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import com.github.houbb.raft.common.exception.RaftRuntimeException;
import com.github.houbb.raft.common.rpc.RpcRequest;

/**
 * @since 1.0.0
 * @param <T> 泛型
 */
public abstract class RaftUserProcessor<T> extends AbstractUserProcessor<T> {

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) {
        throw new RaftRuntimeException("Raft Server not support handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) ");
    }


    @Override
    public String interest() {
        return RpcRequest.class.getName();
    }

}
