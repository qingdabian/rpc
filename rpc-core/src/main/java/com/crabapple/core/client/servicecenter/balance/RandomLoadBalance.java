package com.crabapple.core.client.servicecenter.balance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class RandomLoadBalance implements LoadBalance {
    private final List<String> addresslist=new CopyOnWriteArrayList<>();
    @Override
    public String balance(List<String> addrlist) {
        Random random=new Random();
        int index= random.nextInt(addrlist.size());
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
