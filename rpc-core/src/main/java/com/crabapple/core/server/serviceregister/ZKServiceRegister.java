package com.crabapple.core.server.serviceregister;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

@Slf4j
public class ZKServiceRegister implements ServiceRegister {
    private CuratorFramework client;
    private static final String ROOT_PATH="MyRPC";
    private static final String RETRY="CanRetry";
    public ZKServiceRegister(){
        RetryPolicy policy=new ExponentialBackoffRetry(1000,3);
        this.client= CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(50000)
                .retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        this.client.start();
        log.info("ZKServiceRegister start");
    }
    @Override
    public void regist(String servicename, InetSocketAddress addr,boolean canretry) {
        try{
           if(client.checkExists().forPath("/"+servicename)==null){
               client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/"+servicename);
           }
           String path="/"+servicename+"/"+getServiceAddr(addr);
            if(client.checkExists().forPath(path) != null){
                client.delete().forPath(path);
            }
           client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            if(canretry){
                path="/"+RETRY+"/"+servicename;
                if(client.checkExists().forPath(path) != null){
                    client.delete().forPath(path);
                }
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            }
        }catch (Exception e){
            log.error("ZKServiceRegister regist error",e);
        }
    }

    private String getServiceAddr(InetSocketAddress addr){
        return addr.getHostString()+":"+addr.getPort();
    }
    private InetSocketAddress parseServiceAddr(String addr) {
        String[] arr = addr.split(":");
        return new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
    }
}
