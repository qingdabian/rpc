package com.crabapple.core.client.rpcclient;


import common.message.RpcRequest;
import common.message.RpcResponse;

public interface RpcClient {
    RpcResponse sendRpcRequest(RpcRequest request);
    void close();
}
