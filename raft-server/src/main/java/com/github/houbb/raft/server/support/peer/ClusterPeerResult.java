package com.github.houbb.raft.server.support.peer;

/**
 * 集群节点的结果
 *
 * @since 1.1.0
 */
public class ClusterPeerResult {

    /**
     * 处理成功
     */
    private boolean success;

    public ClusterPeerResult() {
    }

    public ClusterPeerResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ClusterPeerResult{" +
                "success=" + success +
                '}';
    }

}
