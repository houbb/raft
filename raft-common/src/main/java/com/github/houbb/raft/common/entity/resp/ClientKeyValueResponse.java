package com.github.houbb.raft.common.entity.resp;

public class ClientKeyValueResponse extends BaseRaftResponse {

    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
