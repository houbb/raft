package com.github.houbb.raft.server.core;

import com.github.houbb.raft.common.entity.req.dto.LogEntry;

/**
 * 状态机接口
 *
 * 状态机接口，在 Raft 论文中，将数据保存到状态机，作者称之为应用，那么我们也这么命名，说白了，就是将已成功提交的日志应用到状态机中：
 *
 * @since 1.0.0
 */
public interface StateMachine {

    /**
     * 将数据应用到状态机.
     *
     * 原则上,只需这一个方法(apply). 其他的方法是为了更方便的使用状态机.
     * @param logEntry 日志中的数据.
     */
    void apply(LogEntry logEntry);

    /**
     * 获取信息
     * @param key Key
     * @return 结果
     */
    LogEntry get(String key);

    /**
     * 获取信息
     * @param key Key
     * @return 结果
     */
    String getString(String key);

    /**
     * 设置
     * @param key Key
     * @param value 值
     */
    void setString(String key, String value);

    /**
     * 删除
     * @param key Key
     */
    void delString(String... key);


}
