package com.github.houbb.raft.server.bs;

import java.util.Arrays;
import java.util.List;

public class RaftBootstrapTest4 {

    public static void main(String[] args) throws Throwable {
        System.setProperty("serverPort", "8778");

        List<String> clusterList = Arrays.asList("localhost:8775", "localhost:8776", "localhost:8777", "localhost:8778", "localhost:8779");
        RaftBootstrap bootstrap = new RaftBootstrap(8778, clusterList);

        // 启动
        bootstrap.boot();
    }

}
