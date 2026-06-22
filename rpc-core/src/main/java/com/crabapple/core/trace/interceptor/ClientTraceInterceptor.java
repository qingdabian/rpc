package com.crabapple.core.trace.interceptor;

import com.crabapple.core.trace.TraceIdGenerator;
import com.crabapple.core.trace.ZipkinReporter;
import common.trace.TraceContext;

public class ClientTraceInterceptor {
    public static void beforeInvoke(){
        String traceId= TraceContext.getTraceId();
        if(traceId==null){
            traceId= TraceIdGenerator.generateTraceId();
            TraceContext.setTraceId(traceId);
        }
        String spanId=TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId("");

        long startTime=System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTime));
    }
    public static void afterInvoke(String serviceName){
        long endTime=System.currentTimeMillis();
        long startTime=Long.valueOf(TraceContext.getStartTimestamp());
        long duration=endTime-startTime;

        ZipkinReporter.reportClientSpan(TraceContext.getTraceId(),TraceContext.getSpanId(),"client"+serviceName, TraceContext.getParentSpanId(),serviceName,"client",startTime,duration);
        TraceContext.clear();
    }
}
