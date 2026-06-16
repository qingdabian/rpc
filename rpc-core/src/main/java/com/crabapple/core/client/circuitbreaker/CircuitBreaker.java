package com.crabapple.core.client.circuitbreaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    private CircuitBreakerState state=CircuitBreakerState.CLOSE;
    AtomicInteger failurecount=new AtomicInteger(0);
    AtomicInteger successcount=new AtomicInteger(0);
    AtomicInteger requestcount=new AtomicInteger(0);
    private final int failureThreshold;
    private final double halfOpenSuccessRate;//半开状态下的成功率阈值
    private final long restTimePeriod;//重置时间周期
    private long lastFailureTime=0;
    public CircuitBreaker(int failureThreshold,double halfOpenSuccessRate,long restTimePeriod){
        this.failureThreshold=failureThreshold;
        this.halfOpenSuccessRate=halfOpenSuccessRate;
        this.restTimePeriod=restTimePeriod;
    }

    public synchronized boolean allowRequest(){
        long currentTime=System.currentTimeMillis();
        switch(state){
            case OPEN:
                if(currentTime-lastFailureTime>restTimePeriod){
                    state=CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
            case HALF_OPEN:
                requestcount.incrementAndGet();
                return true;
            case CLOSE: return true;
            default:
                return false;
        }
    }
    public synchronized void recordSuccess(){
        if(state==CircuitBreakerState.HALF_OPEN){
            successcount.incrementAndGet();
            if(successcount.get()>halfOpenSuccessRate*requestcount.get()){
                state=CircuitBreakerState.CLOSE;
                resetCounts();
            }
        }else{
            resetCounts();
        }
    }
    public synchronized void recordFailure(){
        failurecount.incrementAndGet();
        lastFailureTime=System.currentTimeMillis();
        if(state==CircuitBreakerState.HALF_OPEN){
            state=CircuitBreakerState.OPEN;
        }else if(failurecount.get()>=failureThreshold){
            state=CircuitBreakerState.OPEN;
        }
    }
    private void resetCounts(){
        failurecount.set(0);
        successcount.set(0);
        requestcount.set(0);
    }
}
