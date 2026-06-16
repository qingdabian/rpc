package com.crabapple.core.server.serviceregister;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    public void regist(String servicename, InetSocketAddress addr,boolean canretry);
}
