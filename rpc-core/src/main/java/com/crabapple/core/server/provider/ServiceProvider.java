package com.crabapple.core.server.provider;



import com.crabapple.core.server.ratelimit.RateLimitProvider;
import com.crabapple.core.server.serviceregister.ServiceRegister;
import com.crabapple.core.server.serviceregister.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String,Object> servicesprovider;
    private RateLimitProvider rateLimitProvider;
//    public ServiceProvider(){
//        servicesprovider=new HashMap<>();
//    }
    private String localAddress;
    private int localPort;
    private ServiceRegister serviceRegister;
    public ServiceProvider(String localAddress,int localPort){
        this.localAddress=localAddress;
        this.localPort=localPort;
        this.serviceRegister=new ZKServiceRegister();
        this.servicesprovider=new HashMap<>();
        this.rateLimitProvider=new RateLimitProvider();
    }
    public void registerService(Object service,boolean canretry){
        Class<?>[] clazzs=service.getClass().getInterfaces();
        for(Class<?> clazz:clazzs) {
            servicesprovider.put(clazz.getName(),service);
            serviceRegister.regist(clazz.getName(),new InetSocketAddress(localAddress,localPort),canretry);
        }
    }

    public Object getService(String interfacename){
        return servicesprovider.get(interfacename);
    }
    public RateLimitProvider getRateLimitProvider(){
        return rateLimitProvider;
    }
}
