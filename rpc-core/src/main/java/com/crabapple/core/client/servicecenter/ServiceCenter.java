package com.crabapple.core.client.servicecenter;

import java.net.InetSocketAddress;

public interface ServiceCenter {
    InetSocketAddress serviceDicovery(String serviceName);
    boolean checkRetry(String servicename);
    void close();
}
