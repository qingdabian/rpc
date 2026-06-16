package com.crabapple.core.server.server;

public interface RpcServer {
    void start(int port);
    void stop();
}
