package com.crabapple.core.server.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
管理令牌桶，不同的服务使用不同的令牌桶
 */
public class RateLimitProvider {
    private Map<String,RateLimit> rateLimitMap=new ConcurrentHashMap<>();
    public RateLimit getRateLimit(String key){
        if(!rateLimitMap.containsKey(key)){
            RateLimit rateLimit=new TokenBucketRateLimitImpl(1,100000);
            rateLimitMap.put(key,rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(key);
    }
}
