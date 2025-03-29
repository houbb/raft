package com.github.houbb.raft.common.rpc;

import java.io.Serializable;

/**
 * rpc 响应
 * @param <T> 泛型
 */
public class RpcResponse<T> implements Serializable {

    private T result;


    public RpcResponse() {
    }

    public RpcResponse(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
