package com.github.houbb.raft.server.core;

import com.github.houbb.raft.common.entity.req.dto.LogEntry;

/**
 * 日志模块
 *
 * @since 1.0.0
 */
public interface LogManager {

    /**
     * 写入
     * @param logEntry 日志
     */
    void write(LogEntry logEntry);

    /**
     * 读取
     * @param index 下标志
     * @return 结果
     */
    LogEntry read(Long index);

    /**
     * 从开始位置删除
     * @param startIndex 开始位置
     */
    void removeOnStartIndex(Long startIndex);

    /**
     * 获取最新的日志
     * @return 日志
     */
    LogEntry getLast();

    /**
     * 获取最新的下标
     * @return 结果
     */
    Long getLastIndex();

}
