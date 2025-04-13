package com.github.houbb.raft.common.constant.enums;

/**
 * 节点的状态枚举
 * @since 1.0.0
 */
public enum NodeStatusEnum {

    FOLLOWER(0, "FOLLOWER"),
    CANDIDATE(1, "CANDIDATE"),
    LEADER(2, "LEADER"),
    ;

    private final int code;
    private final String desc;

    NodeStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
