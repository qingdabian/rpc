package com.crabapple.core.server.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitProvider {
    private Map<String,RateLimit> rateLimitMap=new ConcurrentHashMap<>();
    public RateLimit getRateLimit(String key){
        if(!rateLimitMap.containsKey(key)){
            RateLimit rateLimit=new TokenBucketRateLimitImpl(1000,100);
            rateLimitMap.put(key,rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(key);
    }
}
