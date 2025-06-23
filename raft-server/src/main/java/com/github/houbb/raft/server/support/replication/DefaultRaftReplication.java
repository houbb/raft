package com.github.houbb.raft.server.support.replication;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.constant.RpcRequestCmdConst;
import com.github.houbb.raft.common.constant.enums.NodeStatusEnum;
import com.github.houbb.raft.common.entity.req.AppendLogRequest;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.common.entity.resp.AppendLogResponse;
import com.github.houbb.raft.common.rpc.RpcClient;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.core.impl.DefaultNode;
import com.github.houbb.raft.server.dto.PeerInfoDto;
import com.github.houbb.raft.server.dto.node.NodeInfoContext;
import com.github.houbb.raft.server.support.concurrent.RaftThreadPool;
import com.github.houbb.raft.server.support.peer.PeerManager;

import java.util.LinkedList;
import java.util.concurrent.Future;

public class DefaultRaftReplication implements IRaftReplication {

    private static final Log log = LogFactory.getLog(DefaultNode.class);

    private LogEntry getPreLog(NodeInfoContext nodeInfoContext, LogEntry logEntry) {
        final LogManager logManager = nodeInfoContext.getLogManager();
        LogEntry entry = logManager.read(logEntry.getIndex() - 1);

        if (entry == null) {
            log.warn("get perLog is null , parameter logEntry : {}", logEntry);
            entry = new LogEntry(0L, 0, null);
        }
        return entry;
    }

    @Override
    public Future<Boolean> replication(NodeInfoContext nodeInfoContext, PeerInfoDto peer, LogEntry entry) {
        final RpcClient rpcClient = nodeInfoContext.getRpcClient();
        final PeerManager peerManager = nodeInfoContext.getPeerManager();
        final LogManager logManager = nodeInfoContext.getLogManager();

        return RaftThreadPool.submit(() -> {

            long start = System.currentTimeMillis(), end = start;

            // 20 秒重试时间
            while (end - start < 20 * 1000L) {
                // 基础值
                AppendLogRequest aentryParam = new AppendLogRequest();
                aentryParam.setTerm(nodeInfoContext.getCurrentTerm());
                aentryParam.setServerId(peer.getAddress());
                aentryParam.setLeaderId(peerManager.getSelf().getAddress());

                aentryParam.setLeaderCommit(nodeInfoContext.getCommitIndex());

                // 以我这边为准, 这个行为通常是成为 leader 后,首次进行 RPC 才有意义.
                Long nextIndex = nodeInfoContext.getNextIndexes().get(peer);
                LinkedList<LogEntry> logEntries = new LinkedList<>();
                if (entry.getIndex() >= nextIndex) {
                    for (long i = nextIndex; i <= entry.getIndex(); i++) {
                        LogEntry l = logManager.read(i);
                        if (l != null) {
                            logEntries.add(l);
                        }
                    }
                } else {
                    logEntries.add(entry);
                }
                // 最小的那个日志.
                LogEntry preLog = getPreLog(nodeInfoContext, logEntries.getFirst());
                aentryParam.setPreLogTerm(preLog.getTerm());
                aentryParam.setPrevLogIndex(preLog.getIndex());

                aentryParam.setEntries(logEntries.toArray(new LogEntry[0]));

                RpcRequest request = new RpcRequest();
                request.setObj(aentryParam);
                request.setCmd(RpcRequestCmdConst.A_ENTRIES);
                request.setUrl(peer.getAddress());

                try {
                    AppendLogResponse result = rpcClient.send(request);
                    if (result == null) {
                        return false;
                    }
                    if (result.isSuccess()) {
                        log.info("append follower entry success , follower=[{}], entry=[{}]", peer, aentryParam.getEntries());
                        // update 这两个追踪值
                        nodeInfoContext.getNextIndexes().put(peer, entry.getIndex() + 1);
                        nodeInfoContext.getMatchIndexes().put(peer, entry.getIndex());
                        return true;
                    } else if (!result.isSuccess()) {
                        // 对方比我大
                        if (result.getTerm() > nodeInfoContext.getCurrentTerm()) {
                            log.warn("follower [{}] term [{}] than more self, and my term = [{}], so, I will become follower",
                                    peer, result.getTerm(), nodeInfoContext.getCurrentTerm());

                            nodeInfoContext.setCurrentTerm(result.getTerm());
                            nodeInfoContext.setStatus(NodeStatusEnum.FOLLOWER);

                            return false;
                        } // 没我大, 却失败了,说明 index 不对.或者 term 不对.
                        else {
                            // 递减
                            if (nextIndex == 0) {
                                nextIndex = 1L;
                            }
                            nodeInfoContext.getNextIndexes().put(peer, nextIndex - 1);
                            // log.warn("follower {} nextIndex not match, will reduce nextIndex and retry RPC append, nextIndex : [{}]", peer.getAddress(), nextIndex);
                            // 重来, 直到成功.
                        }
                    }

                    end = System.currentTimeMillis();

                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    // TODO 到底要不要放队列重试?
//                        ReplicationFailModel model =  ReplicationFailModel.newBuilder()
//                            .callable(this)
//                            .logEntry(entry)
//                            .peer(peer)
//                            .offerTime(System.currentTimeMillis())
//                            .build();
//                        replicationFailQueue.offer(model);
                    return false;
                }
            }
            // 超时了,没办法了
            return false;
        });
    }

}
