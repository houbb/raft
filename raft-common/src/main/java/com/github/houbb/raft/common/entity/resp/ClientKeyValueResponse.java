package com.github.houbb.raft.common.entity.resp;

public class ClientKeyValueResponse extends BaseRaftResponse {

    private Object result;

    public ClientKeyValueResponse() {
    }

    public ClientKeyValueResponse(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public static ClientKeyValueResponse fail() {
        return new ClientKeyValueResponse("fail");
    }

    public static ClientKeyValueResponse ok() {
        return new ClientKeyValueResponse("ok");
    }

}
