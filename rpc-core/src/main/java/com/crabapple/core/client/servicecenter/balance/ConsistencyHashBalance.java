package com.crabapple.core.client.servicecenter.balance;

import java.util.*;

public class ConsistencyHashBalance implements LoadBalance {
    private static final int VIRTUAL_NUM=5;
    private SortedMap<Integer,String> shard=new TreeMap<>();
    private List<String> realNodes=new LinkedList<String>();
    private String[] servers=null;

    private void init(List<String> servicelist){
        for(String service:servicelist){
            realNodes.add(service);
            System.out.println("真实存在的节点"+service);
            for(int i=0;i<VIRTUAL_NUM;i++){
                String virtualNode=service+"&&VN"+i;
                int hash=getHash(virtualNode);
                shard.put(hash,virtualNode);
                System.out.println("虚拟节点"+virtualNode+"的hash值为"+hash+"这个节点已经被添加");
            }
        }
    }
    public String getServer(String node,List<String> serviceList){
        if(shard.isEmpty()){
            init(serviceList);
        }
        int hash=getHash(node);
        Integer key=null;
        SortedMap<Integer,String> subMap=shard.tailMap(hash);
        if(subMap.isEmpty()){
            key=shard.lastKey();
        }else{
            key=subMap.firstKey();
        }
        String virtualNode=shard.get(key);
        return virtualNode.substring(0,virtualNode.indexOf("&&"));
    }

    @Override
    public String balance(List<String> addrlist) {
        if(addrlist.isEmpty()||addrlist==null){
            throw new IllegalArgumentException("addrlist is empty or null");
        }
        String random= UUID.randomUUID().toString();
        return getServer(random,addrlist);
    }

    @Override
    public void addNode(String addr) {
       for(int i=0;i<VIRTUAL_NUM;i++){
           if(realNodes.contains(addr)){
                continue;
           }
           realNodes.add(addr);
           String virtualNode=addr+"&&VN"+i;
           int hash=getHash(virtualNode);
           shard.put(hash,virtualNode);
           System.out.println("虚拟节点"+virtualNode+"的hash值为"+hash+"这个节点已经被添加");
       }
    }

    @Override
    public void removeNode(String addr) {
         if(!realNodes.contains(addr)){
            return;
         }
         realNodes.remove(addr);
         for(int i=0;i<VIRTUAL_NUM;i++){
             String virtualNode=addr+"&&VN"+i;
             int hash=getHash(virtualNode);
             shard.remove(hash);
             System.out.println("虚拟节点"+virtualNode+"的hash值为"+hash+"这个节点已经被移除");
         }
    }
    public int getHash(String node){
        final int p=16777619;
        int hash=(int)2166136261L;
        for(int i=0;i<node.length();i++){
            hash=(hash^node.charAt(i))*p;
        }
            hash+=hash<<13;
            hash^=hash>>7;
            hash+=hash<<3;
            hash^=hash>>17;
            hash+=hash<<5;
            if(hash<0){
                hash=-hash;
            }
            return hash;
    }
}
