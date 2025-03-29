package com.github.houbb.raft.common.constant;

/**
 * 请求常量
 * @since 1.0.0
 */
public class RpcRequestCmdConst {

    /** 请求投票 */
    public static final int R_VOTE = 0;
    /** 附加日志 */
    public static final int A_ENTRIES = 1;
    /** 客户端 */
    public static final int CLIENT_REQ = 2;

    /** 配置变更. add */
    public static final int CHANGE_CONFIG_ADD = 3;
    /** 配置变更. remove */
    public static final int CHANGE_CONFIG_REMOVE = 4;

}
