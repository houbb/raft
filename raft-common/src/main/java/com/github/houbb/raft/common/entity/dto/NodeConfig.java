/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.github.houbb.raft.common.entity.dto;

import java.util.List;

/**
 *
 * 节点配置
 */
public class NodeConfig {

    /** 自身 selfPort */
    public int selfPort;

    /** 所有节点地址. */
    public List<String> peerAddressList;

    /**
     *  状态快照存储类型
     *  TODO: 这个可以不用管，直接根据实现类来处理。
     */
    public String stateMachineSaveType;

    public int getSelfPort() {
        return selfPort;
    }

    public void setSelfPort(int selfPort) {
        this.selfPort = selfPort;
    }

    public List<String> getPeerAddressList() {
        return peerAddressList;
    }

    public void setPeerAddressList(List<String> peerAddressList) {
        this.peerAddressList = peerAddressList;
    }

    public String getStateMachineSaveType() {
        return stateMachineSaveType;
    }

    public void setStateMachineSaveType(String stateMachineSaveType) {
        this.stateMachineSaveType = stateMachineSaveType;
    }
}
