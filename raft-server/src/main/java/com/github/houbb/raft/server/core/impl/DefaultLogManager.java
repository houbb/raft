package com.github.houbb.raft.server.core.impl;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.server.core.LogManager;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 日志管理
 *
 * @since 1.0.0
 */
public class DefaultLogManager implements LogManager {

    private static final Log log = LogFactory.getLog(DefaultConsensus.class);

    // double-lock
    private static class DefaultLogsLazyHolder {
        private static final DefaultLogManager INSTANCE = new DefaultLogManager();
    }
    public static LogManager getInstance() {
        return DefaultLogsLazyHolder.INSTANCE;
    }

    /**
     * 数据目录
     */
    private final String dbDir;
    /**
     * 日志目录
     */
    private final String logsDir;

    private RocksDB logDb;

    private final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();

    private ReentrantLock lock = new ReentrantLock();

    private DefaultLogManager(String dbDir) {
        ArgUtil.notEmpty(dbDir, "dbDir");

        this.dbDir = dbDir;
        this.logsDir = dbDir + "/logManager";

        this.init();
    }
    private DefaultLogManager() {
        this("./rocksDB-raft/" + System.getProperty("serverPort"));
    }

    private void init() {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);

        File file = new File(logsDir);
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            log.info("[Rate] DefaultLogManager make a new dir : " + logsDir);
        }
        try {
            logDb = RocksDB.open(options, logsDir);
        } catch (RocksDBException e) {
            log.error("[Rate] DefaultLogManager init meet ex", e.getMessage());
        }
    }

    @Override
    public void write(LogEntry logEntry) {
        boolean success = false;
        boolean result;
        try {
            result = lock.tryLock(3000, MILLISECONDS);
            if (!result) {
                throw new RuntimeException("write fail, tryLock fail.");
            }
            logEntry.setIndex(getLastIndex() + 1);
            logDb.put(logEntry.getIndex().toString().getBytes(), JSON.toJSONBytes(logEntry));
            success = true;
            log.info("DefaultLogModule write rocksDB success, logEntry info : [{}]", logEntry);
        } catch (RocksDBException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (success) {
                updateLastIndex(logEntry.getIndex());
            }
            lock.unlock();
        }
    }

    @Override
    public LogEntry read(Long index) {
        try {
            byte[] result = logDb.get(convert(index));
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {
        boolean success = false;
        int count = 0;
        boolean tryLock;
        try {
            tryLock = lock.tryLock(3000, MILLISECONDS);
            if (!tryLock) {
                throw new RuntimeException("tryLock fail, removeOnStartIndex fail");
            }
            for (long i = startIndex; i <= getLastIndex(); i++) {
                logDb.delete(String.valueOf(i).getBytes());
                ++count;
            }
            success = true;
            log.warn("rocksDB removeOnStartIndex success, count={} startIndex={}, lastIndex={}", count, startIndex, getLastIndex());
        } catch (InterruptedException | RocksDBException e) {
            throw new RuntimeException(e);
        } finally {
            if (success) {
                updateLastIndex(getLastIndex() - count);
            }
            lock.unlock();
        }
    }

    @Override
    public LogEntry getLast() {
        try {
            byte[] result = logDb.get(convert(getLastIndex()));
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getLastIndex() {
        byte[] lastIndex;
        try {
            lastIndex = logDb.get(LAST_INDEX_KEY);
            if (lastIndex == null) {
                lastIndex = "-1".getBytes();
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return Long.valueOf(new String(lastIndex));
    }

    private byte[] convert(Long key) {
        return key.toString().getBytes();
    }

    // on lock
    private void updateLastIndex(Long index) {
        try {
            // overWrite
            logDb.put(LAST_INDEX_KEY, index.toString().getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

}
