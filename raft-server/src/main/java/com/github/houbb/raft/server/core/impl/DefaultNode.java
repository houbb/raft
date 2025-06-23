package com.github.houbb.raft.server.core.impl;

import com.alibaba.fastjson.JSON;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.RpcRequestCmdConst;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.dto.NodeConfig;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.ClientKeyValueRequest;
import com.github.houbb.raft.common.entity.req.VoteRequest;
import com.github.houbb.raft.common.entity.req.dto.Command;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.entity.resp.ClientKeyValueResponse;
import com.github.houbb.raft.common.entity.resp.VoteResponse;
import com.github.houbb.raft.common.rpc.DefaultRpcClient;
import com.github.houbb.raft.common.rpc.RpcClient;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.server.core.Consensus;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.core.Node;
import com.github.houbb.raft.server.core.StateMachine;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;
import com.github.houbb.raft.server.rpc.DefaultRpcServer;
import com.github.houbb.raft.server.rpc.RpcServer;
import com.github.houbb.raft.server.support.concurrent.RaftThreadPool;
import com.github.houbb.raft.server.support.hearbeat.HeartbeatTask;
import com.github.houbb.raft.server.support.peer.PeerManager;
import com.github.houbb.raft.server.support.replication.DefaultRaftReplication;
import com.github.houbb.raft.server.support.replication.IRaftReplication;
import com.github.houbb.raft.server.support.vote.VoteTask;
import com.github.houbb.raft.server.util.InnerFutureList;
import com.github.houbb.raft.server.util.InnerPeerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DefaultNode implements Node {

    private static final Log log = LogFactory.getLog(DefaultNode.class);

    /**
     * 配置信息
     * @since 1.0.0
     */
    private NodeConfig nodeConfig;

    /**
     * 上下文信息
     * @since 1.0.0
     */
    private final NodeInfoContext nodeInfoContext = new NodeInfoContext();

    @Override
    public void setConfig(NodeConfig config) {
        this.nodeConfig = config;
    }

    @Override
    public void init() throws Throwable {
        nodeInfoContext.setRunning(true);

        // 暂时不考虑节点的变化

        // 基本初始化
        RpcClient rpcClient = new DefaultRpcClient();
        rpcClient.init();
        RpcServer rpcServer = new DefaultRpcServer(this, nodeConfig.getSelfPort());
        rpcServer.init();

        // 有没有更好的初始化方法？
        StateMachine stateMachine = DefaultStateMachine.getInstance();
        LogManager logManager = DefaultLogManager.getInstance();

        PeerManager peerManager = InnerPeerUtil.initPeerManager(nodeConfig);

        IRaftReplication raftReplication = new DefaultRaftReplication();
        nodeInfoContext.setRaftReplication(raftReplication);

        nodeInfoContext.setPeerManager(peerManager);
        nodeInfoContext.setRpcClient(rpcClient);
        nodeInfoContext.setRpcServer(rpcServer);
        nodeInfoContext.setStateMachine(stateMachine);
        nodeInfoContext.setLogManager(logManager);
        Consensus consensus = new DefaultConsensus(nodeInfoContext);
        nodeInfoContext.setConsensus(consensus);

        //2. 初始化调度
        RaftThreadPool.scheduleAtFixedRate(new HeartbeatTask(nodeInfoContext), 1000, 1000);
        // 投票感觉没必要这么频繁
        RaftThreadPool.scheduleAtFixedRate(new VoteTask(nodeInfoContext), 6000, 5000);
    }

    @Override
    public void destroy() throws Throwable {
        nodeInfoContext.getRpcClient().destroy();
        nodeInfoContext.getRpcServer().destroy();
        nodeInfoContext.getStateMachine().destroy();

        // 状态
        nodeInfoContext.setRunning(false);
    }

    @Override
    public VoteResponse handlerRequestVote(VoteRequest param) {
        // log.info("handlerRequestVote req={}", param);
        return nodeInfoContext.getConsensus().vote(param);
    }

    @Override
    public AppendLogResponse handlerAppendEntries(AppendLogRequest param) {
        // log.info("handlerAppendEntries req={}", JSON.toJSONString(param));
        return nodeInfoContext.getConsensus().appendLog(param);
    }

    /**
     * 客户端的每一个请求都包含一条被复制状态机执行的指令。
     * 领导人把这条指令作为一条新的日志条目附加到日志中去，然后并行的发起附加条目 RPCs 给其他的服务器，让他们复制这条日志条目。
     * 当这条日志条目被安全的复制（下面会介绍），领导人会应用这条日志条目到它的状态机中然后把执行的结果返回给客户端。
     * 如果跟随者崩溃或者运行缓慢，再或者网络丢包，
     * 领导人会不断的重复尝试附加日志条目 RPCs （尽管已经回复了客户端）直到所有的跟随者都最终存储了所有的日志条目。
     *
     * @param request 请求
     * @return 结果
     */
    @Override
    public ClientKeyValueResponse handlerClientRequest(ClientKeyValueRequest request) {
        log.info("[Raft] handlerClientRequest request={}", request);

        // 重定向到主节点
        final PeerManager peerManager = nodeInfoContext.getPeerManager();
        if (nodeInfoContext.getStatus() != NodeStatusEnum.LEADER) {
            log.warn("[Raft] I not am leader , only invoke redirect method, leader address : {}, my address : {}", peerManager.getLeader(), peerManager.getSelf());
            return redirect(request);
        }

        // 读取操作？
        final StateMachine stateMachine = nodeInfoContext.getStateMachine();
        if (request.getType() == ClientKeyValueRequest.GET) {
            LogEntry logEntry = stateMachine.get(request.getKey());
            if (logEntry != null) {
                return new ClientKeyValueResponse(logEntry);
            }
            return new ClientKeyValueResponse(null);
        }

        // 处理
        LogEntry logEntry = new LogEntry();
        Command command = new Command(request.getKey(), request.getValue());
        logEntry.setCommand(command);
        logEntry.setTerm(nodeInfoContext.getCurrentTerm());
        // todo.. index 呢？
        final LogManager logManager = nodeInfoContext.getLogManager();
        // 预提交到本地日志, TODO 预提交
        logManager.write(logEntry);
        log.info("write logModule success, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());

        final AtomicInteger success = new AtomicInteger(0);
        List<Future<Boolean>> futureList = new ArrayList<>();

        int count = 0;
        //  复制到其他机器
        final IRaftReplication raftReplication = nodeInfoContext.getRaftReplication();
        for (PeerInfoDto peer : peerManager.getPeersWithOutSelf()) {
            // TODO check self and RaftThreadPool
            count++;
            // 并行发起 RPC 复制.
            Future<Boolean> future = raftReplication.replication(nodeInfoContext, peer, logEntry);
            futureList.add(future);
        }

        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = InnerFutureList.getRPCAppendResult(futureList, latch);

        try {
            latch.await(4000, MILLISECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        // 如果存在一个满足N > commitIndex的 N，并且大多数的matchIndex[i] ≥ N成立，
        // 并且log[N].term == currentTerm成立，那么令 commitIndex 等于这个 N （5.3 和 5.4 节）
        List<Long> matchIndexList = new ArrayList<>(nodeInfoContext.getMatchIndexes().values());
        // 小于 2, 没有意义
        int median = 0;
        if (matchIndexList.size() >= 2) {
            Collections.sort(matchIndexList);
            median = matchIndexList.size() / 2;
        }
        Long N = matchIndexList.get(median);
        if (N > nodeInfoContext.getCommitIndex()) {
            LogEntry entry = logManager.read(N);
            if (entry != null && entry.getTerm() == nodeInfoContext.getCurrentTerm()) {
                nodeInfoContext.setCommitIndex(N);
            }
        }

        //  响应客户端(成功一半)
        if (success.get() >= (count / 2)) {
            // 更新
            nodeInfoContext.setCommitIndex(logEntry.getIndex());

            //  应用到状态机
            stateMachine.apply(logEntry);
            nodeInfoContext.setLastApplied(nodeInfoContext.getCommitIndex());

            log.info("success apply local state machine,  logEntry info : {}", logEntry);
            // 返回成功.
            return ClientKeyValueResponse.ok();
        } else {
            // 回滚已经提交的日志.
            logManager.removeOnStartIndex(logEntry.getIndex());
            log.warn("fail apply local state  machine,  logEntry info : {}", logEntry);
            // TODO 不应用到状态机,但已经记录到日志中.由定时任务从重试队列取出,然后重复尝试,当达到条件时,应用到状态机.
            // 这里应该返回错误, 因为没有成功复制过半机器.
            return ClientKeyValueResponse.fail();
        }
    }


    @Override
    public ClientKeyValueResponse redirect(ClientKeyValueRequest request) {
        final String leaderAddress = nodeInfoContext.getPeerManager().getLeader().getAddress();
        RpcRequest r = new RpcRequest();
        r.setObj(request);
        r.setCmd(RpcRequestCmdConst.CLIENT_REQ);
        r.setUrl(leaderAddress);

        log.info("[Raft] node redirect req={}", r);
        return nodeInfoContext.getRpcClient().send(r);
    }


}
