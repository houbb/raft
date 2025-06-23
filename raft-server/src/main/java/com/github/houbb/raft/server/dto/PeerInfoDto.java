package com.github.houbb.raft.server.dto;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "PeerInfoDto{" +
                "address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfoDto that = (PeerInfoDto) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(address);
    }

}
