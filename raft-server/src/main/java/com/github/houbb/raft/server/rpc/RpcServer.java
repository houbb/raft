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
package com.github.houbb.raft.server.rpc;


import com.github.houbb.raft.common.core.LifeCycle;
import com.github.houbb.raft.common.rpc.RpcRequest;
import com.github.houbb.raft.common.rpc.RpcResponse;

/**
 * rpc 服务端
 *
 * @author houbb
 */
public interface RpcServer extends LifeCycle {

    /**
     * 处理请求.
     * @param request 请求参数.
     * @return 返回值.
     */
    RpcResponse<?> handlerRequest(RpcRequest request);

}
