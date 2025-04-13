package com.github.houbb.raft.server.dto;

/**
 * 节点信息
 */
public class PeerInfoDto {

    /**
     * 地址信息，暂时避免使用 host+port，方便后续域名等拓展？
     */
    private String address;

    public PeerInfoDto() {
    }

    public PeerInfoDto(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
