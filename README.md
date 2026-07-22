# krpc — 轻量级 RPC 通信框架

基于 **Java + Netty + ZooKeeper + Zipkin** 自实现的 RPC 框架，涵盖完整通信链路与服务治理能力。

## 特性

- **Netty 长连接** — 自定义 LengthField 帧协议解决 TCP 粘包/半包，心跳保活（写空闲 20s 发心跳，读空闲 30s 超时自动断开）
- **服务注册发现** — ZooKeeper 存储服务地址，Watcher 监听节点变化实时同步本地缓存
- **负载均衡** — 一致性哈希（FNV Hash + 虚拟节点）
- **序列化可插拔** — SPI 机制热切换，内置 JSON / Kryo / Protostuff / Hessian / Java 5 种方案
- **容错治理** — 三态熔断器（CLOSE → OPEN → HALF_OPEN）、令牌桶限流、指数退避重试
- **全链路追踪** — Zipkin + Brave，自定义编解码器在二进制层透传 traceId/spanId/parentSpanId
- **并发安全** — requestId + CompletableFuture 异步匹配，解决单 Channel 多线程响应串台

## 模块结构

```
rpc
├── rpc-api         # 服务接口 & POJO（暴露给 consumer 和 provider）
├── rpc-common      # 公共组件：消息协议、序列化器、SPI 加载器
├── rpc-core        # 框架核心：客户端/服务端、注册中心、负载均衡、容错
├── rpc-comcumer    # 消费者示例 & 压测工具
└── rpc-provider    # 服务提供者示例
```

## 核心架构

```
┌──────────┐         ┌──────────────┐         ┌──────────┐
│ Consumer │ ◄─RPC──►│  ZooKeeper   │◄─RPC──►│ Provider │
│ (Client) │         │  (Registry)  │         │ (Server) │
└──────────┘         └──────────────┘         └──────────┘
      │                                              │
      │          ┌──────────────┐                    │
      └──────────►   Zipkin     ◄────────────────────┘
                 │  (Tracing)   │
                 └──────────────┘
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- ZooKeeper
- Zipkin

### 1. 启动 ZooKeeper

```bash
# 确保 ZK 在 2181 端口运行
```

### 2. 启动服务端

```java
// rpc-provider: ProviderTest.java
RpcApplication.init();
UserService userService = new UserServiceImpl();
ServiceProvider serviceProvider = new ServiceProvider(ip, port);
serviceProvider.registerService(userService, true);
RpcServer rpcServer = new NettyRpcServer(serviceProvider);
rpcServer.start(port);
```

### 3. 调用服务

```java
// rpc-comcumer: TestClient.java
ClientProxy clientProxy = new ClientProxy(0);     // 0=Netty, 1=Simple
UserService userService = clientProxy.getProxy(UserService.class);

User user = userService.getUserById(1);
System.out.println(user);
```

### 4. 压测（可选）

```java
// rpc-comcumer: StressTest.java
// 5 级并发递增：1 → 4 → 8 → 16 → 32 线程
```

## 压测数据

| 并发 | QPS | P50 | P90 | P99 | 错误率 |
|------|-----|-----|-----|-----|--------|
| 32 线程 | 1503 | 18ms | 35ms | 58ms | 0% |

> 单机 Mac/Windows，限流器 TokenBucket(1ms, 100000)，JSON 序列化

## 通信协议

```
┌──────────┬──────────┬──────────────┐
│ Length(4)│ Header   │ Body         │
│ 0x0000.. │ RpcReq/  │ Serialized   │
│          │ RpcResp  │ Object       │
└──────────┴──────────┴──────────────┘
```

- 基于 `LengthFieldBasedFrameDecoder` 帧定界
- `MyEncoder` / `MyDecoder` 完成编解码，同时在二进制层透传 trace 上下文

## 依赖

| 组件 | 版本 | 用途 |
|------|------|------|
| Netty | 4.1.51 | 网络通信 |
| ZooKeeper (Curator) | 5.1.0 | 服务注册发现 |
| Zipkin (Brave + OkHttp) | 3.4.0 | 分布式追踪 |
| Hystrix | 1.5.18 | 熔断降级 |
| Guava Retrying | 2.0.0 | 失败重试 |
| Kryo / Protostuff / Hessian | 4.0.2 / 1.7.4 / 4.0.66 | 序列化方案 |
| Fastjson2 | 2.0.62 | JSON 序列化 |

