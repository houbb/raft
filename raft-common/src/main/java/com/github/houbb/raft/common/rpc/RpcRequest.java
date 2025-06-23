package com.github.houbb.raft.common.rpc;

import java.io.Serializable;

/**
 * 请求
 */
public class RpcRequest implements Serializable {

    /** 请求类型 */
    private int cmd = -1;

    /**
     * param
     */
    private Object obj;

    /**
     * 地址
     */
    private String url;


    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "cmd=" + cmd +
                ", obj=" + obj +
                ", url='" + url + '\'' +
                '}';
    }

}
