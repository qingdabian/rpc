package com.crabapple.core.server.ratelimit;
/*
使用令牌桶算法实现限流
 */
public class TokenBucketRateLimitImpl implements RateLimit {
    //令牌产生速率
    private int rate;
    //桶的最大容量
    private int capacity;
    private volatile int curCapacity;
    private volatile long timeStamp=System.currentTimeMillis();
    public TokenBucketRateLimitImpl(int rate,int capacity){
        this.rate=rate;
        this.capacity=capacity;
        this.curCapacity=capacity;
        this.timeStamp=System.currentTimeMillis();
    }
    @Override
    public boolean getToken() {
        synchronized (this){
            if(curCapacity>0){
                curCapacity--;
                return true;
            }
            long curr=System.currentTimeMillis();
            if(curr-timeStamp>=rate){
                if((curr-timeStamp)/rate>=2){
                    curCapacity+=(int)((curr-timeStamp)/rate)-1;
                }
                if(curCapacity>capacity){
                    curCapacity=capacity;
                }
                timeStamp=curr;
                return true;
            }
            return false;
        }
    }
}
