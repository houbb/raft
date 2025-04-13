package com.github.houbb.raft.server.support.peer;

import com.github.houbb.raft.server.dto.PeerInfoDto;

import java.util.ArrayList;
import java.util.List;

public class PeerManager {

    /**
     * 全部的节点
     */
    private List<PeerInfoDto> list = new ArrayList<>();

    /**
     * 管理这
     */
    private volatile PeerInfoDto leader;

    /**
     * 自己
     */
    private volatile PeerInfoDto self;


    public List<PeerInfoDto> getList() {
        return list;
    }

    public void setList(List<PeerInfoDto> list) {
        this.list = list;
    }

    public PeerInfoDto getLeader() {
        return leader;
    }

    public void setLeader(PeerInfoDto leader) {
        this.leader = leader;
    }

    public PeerInfoDto getSelf() {
        return self;
    }

    public void setSelf(PeerInfoDto self) {
        this.self = self;
    }
}
