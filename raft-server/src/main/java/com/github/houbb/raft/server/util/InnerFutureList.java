package com.github.houbb.raft.server.util;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.server.support.concurrent.RaftThreadPool;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class InnerFutureList {

    private static final Log log = LogFactory.getLog(InnerFutureList.class);

    public static List<Boolean> getRPCAppendResult(List<Future<Boolean>> futureList, CountDownLatch latch) {
        final List<Boolean> resultList = new CopyOnWriteArrayList<>();
        for (Future<Boolean> future : futureList) {
            RaftThreadPool.execute(() -> {
                try {
                    resultList.add(future.get(3000, MILLISECONDS));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    resultList.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        return resultList;
    }

}
