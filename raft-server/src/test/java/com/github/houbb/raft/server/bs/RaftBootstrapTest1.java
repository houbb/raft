package com.github.houbb.raft.server.bs;

import java.util.Arrays;
import java.util.List;

public class RaftBootstrapTest1 {

    public static void main(String[] args) throws Throwable {
        System.setProperty("serverPort", "8775");

        List<String> clusterList = Arrays.asList("localhost:8775", "localhost:8776", "localhost:8777", "localhost:8778", "localhost:8779");
        RaftBootstrap bootstrap = new RaftBootstrap(8775, clusterList);

        // 启动
        bootstrap.boot();
    }

}
