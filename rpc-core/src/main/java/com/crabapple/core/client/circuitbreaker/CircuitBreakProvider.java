package com.crabapple.core.client.circuitbreaker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakProvider {
    private Map<String,CircuitBreaker> circuitBreakers=new ConcurrentHashMap<>();
    public synchronized CircuitBreaker getCircuitBreaker(String serviceName){
        CircuitBreaker circuitBreaker;
        if(circuitBreakers.containsKey(serviceName)){
            circuitBreaker=circuitBreakers.get(serviceName);
        }else{
            System.out.println("服务:"+serviceName+"创建一个新的熔断器");
            circuitBreaker=new CircuitBreaker(10,0.5,1000);
            circuitBreakers.put(serviceName,circuitBreaker);
        }
        return circuitBreaker;
    }
}
