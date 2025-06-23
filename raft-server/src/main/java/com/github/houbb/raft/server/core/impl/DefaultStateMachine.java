package com.github.houbb.raft.server.core.impl;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.raft.common.entity.req.dto.Command;
import com.github.houbb.raft.common.entity.req.dto.LogEntry;
import com.github.houbb.raft.server.core.LogManager;
import com.github.houbb.raft.server.core.StateMachine;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;

/**
 * 默认的状态机
 * @since 1.0.0
 */
public class DefaultStateMachine implements StateMachine {

    private static final Log log = LogFactory.getLog(DefaultStateMachine.class);

    private String dbDir;
    private String stateMachineDir;
    private RocksDB machineDb;

    // double-lock
    private static class DefaultStateMachineLazyHolder {
        private static final DefaultStateMachine INSTANCE = new DefaultStateMachine();
    }
    public static StateMachine getInstance() {
        return DefaultStateMachine.DefaultStateMachineLazyHolder.INSTANCE;
    }

    private DefaultStateMachine(String dbDir) {
        ArgUtil.notEmpty(dbDir, "dbDir");
        this.dbDir = dbDir;
        this.stateMachineDir = this.dbDir + "/stateMachine";

        innerInit();
    }

    private DefaultStateMachine() {
        this("./rocksDB-raft/" + System.getProperty("serverPort"));
    }

    private void innerInit() {
        RocksDB.loadLibrary();

        File file = new File(stateMachineDir);
        boolean success = false;

        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            log.warn("make a new dir : " + stateMachineDir);
        }
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            machineDb = RocksDB.open(options, stateMachineDir);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void apply(LogEntry logEntry) {
        try {
            Command command = logEntry.getCommand();

            if (command == null) {
                // 忽略空日志
                log.warn("insert no-op log, logEntry={}", logEntry);
                return;
            }
            String key = command.getKey();
            machineDb.put(key.getBytes(), JSON.toJSONBytes(logEntry));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LogEntry get(String key) {
        try {
            byte[] result = machineDb.get(key.getBytes());
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntry.class);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getString(String key) {
        try {
            byte[] bytes = machineDb.get(key.getBytes());
            if (bytes != null) {
                return new String(bytes);
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public void setString(String key, String value) {
        try {
            machineDb.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delString(String... key) {
        try {
            for (String s : key) {
                machineDb.delete(s.getBytes());
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() throws Throwable {

    }

    @Override
    public void destroy() throws Throwable {
        machineDb.close();
        log.info("[Rate] stateMachine destroy success");
    }

}
