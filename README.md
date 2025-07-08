# raft

[raft](https://github.com/houbb/raft) raft 算法的基本 java 实现。

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.houbb/raft/badge.svg)](http://mvnrepository.com/artifact/com.github.houbb/raft)
[![Open Source Love](https://badges.frapsoft.com/os/v2/open-source.svg?v=103)](https://github.com/houbb/raft)
[![](https://img.shields.io/badge/license-Apache2-FF0080.svg)](https://github.com/houbb/raft/blob/master/LICENSE.txt)

如果有一些疑难杂症，可以加入：[技术交流群](https://mp.weixin.qq.com/s/rkSvXxiiLGjl3S-ZOZCr0Q)

## 创作目的

大家好，我是老马。

主要是为了学习一下 raft，天天听别人吹牛听烦了。

用了很久也不懂，就学习一下。

## 特性

- leader 选举
- 日志复制
- 成员变更
- 日志压缩


## 变更日志

[CHANGE_LOG.md](https://github.com/houbb/raft/blob/master/CHANGE_LOG.md)

# 入门

## quick start

🔥🔥🔥🔥🔥 注意：该项目仅支持 oracle jdk8 启动。

🔴🔴🔴🔴🔴 注意：idea 需要安装 lombok 插件。

#### 验证 "leader 选举"

1. 启动RaftBootstrapTest1、RaftBootstrapTest2、RaftBootstrapTest3、RaftBootstrapTest4、RaftBootstrapTest5
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 观察控制台, 约 6 秒后, 会发生选举事件,此时,会产生一个 leader. 而  leader 会立刻发送心跳维持自己的地位.
4. 如果leader 的端口是  8775, 使用 idea 关闭 8775 端口，模拟节点挂掉, 大约 15 秒后, 会重新开始选举, 并且会在剩余的 4 个节点中,产生一个新的 leader.  并开始发送心跳日志。

#### 验证"日志复制"

##### 正常状态下

1. 启动RaftBootstrapTest1、RaftBootstrapTest2、RaftBootstrapTest3、RaftBootstrapTest4、RaftBootstrapTest5
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 使用客户端写入 kv 数据.
4. 杀掉所有节点, 使用 junit test 读取每个 rocksDB 的值, 验证每个节点的数据是否一致.

##### 非正常状态下

1. 启动RaftBootstrapTest1、RaftBootstrapTest2、RaftBootstrapTest3、RaftBootstrapTest4、RaftBootstrapTest5
2. 依次启动 5 个 RaftNodeBootStrap 节点, 端口分别是 8775，8776， 8777, 8778, 8779.
3. 使用客户端写入 kv 数据.
4. 杀掉 leader （假设是 8775）.
5. 再次写入数据.
6. 重启 8775.
7. 关闭所有节点, 读取 RocksDB 验证数据一致性.

# 拓展阅读

[raft-07-java 如何编写一个 Raft 分布式 KV 存储 lu-raft-kv](https://houbb.github.io/2022/07/09/sofastack-sofajraft-07-raft-impl-in-java-lu)

# 后期 ROAD-MAP

- [ ] 成员变更 add/remove

- [ ] 日志压缩

- [ ] 输出的信息优化

- [ ] 企业级 raft + 实际使用

# 技术鸣谢

主要参考项目 https://github.com/stateIs0/lu-raft-kv

目前用于学习，后续希望可以拓展简化自己使用