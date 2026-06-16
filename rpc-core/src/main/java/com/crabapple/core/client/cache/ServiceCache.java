package com.crabapple.core.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ServiceCache {
    private static Map<String, List<String>> cache=new ConcurrentHashMap<>();
    public void addServiceToCache(String serviceName,String address){
        if(cache.containsKey(serviceName)){
            List<String> addresses=cache.get(serviceName);
            addresses.add(address);
            log.info("add service {} to cache",serviceName,addresses);
        }
        else{
            List<String> addresses=new ArrayList<>();
            addresses.add(address);
            cache.put(serviceName,addresses);
            log.info("add service {} to cache",serviceName,addresses);
        }
    }
    public void replaceServiceToCache(String serviceName,String oldAddress,String newAddress){
        if(cache.containsKey(serviceName)){
            List<String> addresses=cache.get(serviceName);
            addresses.remove(oldAddress);
            addresses.add(newAddress);
            log.info("replace service {} to cache",serviceName,addresses);
        }else{
            log.error("service {} is not in cache",serviceName);
        }
    }
    public List<String> getServiceAddresses(String serviceName){
        if(cache.containsKey(serviceName)){
            return cache.get(serviceName);
        }
        return Collections.emptyList();
    }
    public void removeServiceFromCache(String serviceName){
        if(cache.containsKey(serviceName)){
            cache.remove(serviceName);
            log.info("remove service {} from cache",serviceName);
        }else{
            log.error("service {} is not in cache",serviceName);
        }
    }
    public void delete(String serviceName,String address){
        List<String> addrlist=cache.get(serviceName);
        if(addrlist!=null&&addrlist.contains(address)){
            addrlist.remove(address);
            log.info("delete service {} from cache",serviceName,addrlist);
            if(addrlist.isEmpty()){
                cache.remove(serviceName);
            }
        }else{
            log.warn("service {} is not in cache",serviceName);
        }
    }
}
