package com.crabapple.core.client.servicecenter.balance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class RoundLoadBalance implements LoadBalance {
    private final List<String> addresslist=new CopyOnWriteArrayList<>();
    private AtomicInteger choose=new AtomicInteger(0);
    @Override
    public String balance(List<String> addrlist) {
        if(addrlist.isEmpty()||addrlist==null){
            throw new IllegalArgumentException("addrlist is empty or null");
        }
        int index=choose.getAndIncrement()%addrlist.size();
        System.out.println("选择了主机"+addrlist.get(index)+"的服务");
        return addrlist.get(index);
    }

    @Override
    public void addNode(String addr) {
        addresslist.add(addr);
        System.out.println("添加主机"+addr+"的负载均衡服务");
    }

    @Override
    public void removeNode(String addr) {
        addresslist.remove(addr);
        System.out.println("移除主机"+addr+"的负载均衡服务");
    }
}
