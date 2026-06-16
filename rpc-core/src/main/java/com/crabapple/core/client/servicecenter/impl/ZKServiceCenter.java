package com.crabapple.core.client.servicecenter.impl;


import com.crabapple.core.client.cache.ServiceCache;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.ZKWatcher.watchZK;
import com.crabapple.core.client.servicecenter.balance.ConsistencyHashBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceCenter implements ServiceCenter {
    private CuratorFramework client;
    private ServiceCache serviceCache;
    private static final String ROOT_PATH="MyRPC";
    private watchZK zkWatcher;
    private static final String RETRY="CanRetry";
    private ConsistencyHashBalance consistencyHashBalance=new ConsistencyHashBalance();
    public ZKServiceCenter(){
        RetryPolicy policy=new ExponentialBackoffRetry(1000,3);
        this.client= CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(50000).retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        client.start();
        serviceCache=new ServiceCache();
        zkWatcher=new watchZK(client,serviceCache);
        zkWatcher.watch(ROOT_PATH);
        log.info("ZKServiceCenter start");
    }
    @Override
    public InetSocketAddress serviceDicovery(String serviceName) {
        List<String> services=serviceCache.getServiceAddresses(serviceName);
        if(services!=null&&!services.isEmpty()){
            String server=consistencyHashBalance.balance(services);
            return parseAddress(server);
        }else{
            try{
                List<String> strings=client.getChildren().forPath("/"+serviceName);
                String s=strings.get(0);
                serviceCache.addServiceToCache(serviceName,s);
                return parseAddress(s);
            }catch (Exception e){
                log.error("serviceDicovery error",e);
            }
        }
        return null;
    }

    @Override
    public boolean checkRetry(String servicename) {
        try{
            List<String> services=client.getChildren().forPath("/"+RETRY);
            for(String s:services){
                if(s.equals(servicename)){
                    return true;
                }
            }
        }catch (Exception e){
            log.error("checkRetry error",e);
        }
        return false;
    }

    @Override
    public void close() {
        client.close();
    }

    private InetSocketAddress parseAddress(String address){
        String[] str=address.split(":");
        return new InetSocketAddress(str[0],Integer.parseInt(str[1]));
    }
}
