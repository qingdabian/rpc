package com.crabapple.core.client.retry;


import com.crabapple.core.client.rpcclient.RpcClient;
import com.github.rholder.retry.*;
import common.message.RpcRequest;
import common.message.RpcResponse;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class GuavaRetry {
    private RpcClient rpcClient;
    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        this.rpcClient=rpcClient;
        Retryer<RpcResponse> retryer= RetryerBuilder
                .<RpcResponse>newBuilder()
                .retryIfException()
                .retryIfResult(response-> Objects.equals(response.getCode(),500))
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("第"+attempt.getAttemptNumber()+"次重试");
                    }
                }).build();
        try{
            return retryer.call(()->rpcClient.sendRpcRequest(request));
        }catch (Exception e){
            e.printStackTrace();
        }
        return RpcResponse.error();
    }
}
