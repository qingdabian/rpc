package com.crabapple.core.trace.interceptor;

import com.crabapple.core.trace.TraceIdGenerator;
import com.crabapple.core.trace.ZipkinReporter;
import common.trace.TraceContext;

public class ServerTraceInterceptor {
    public static void beforeInvoke(){
        String traceId= TraceContext.getTraceId();
        String parentSpanId=TraceContext.getParentSpanId();
        String spanId=TraceIdGenerator.generateSpanId();
        TraceContext.setSpanId(spanId);
        TraceContext.setParentSpanId(parentSpanId);
        TraceContext.setTraceId(traceId);
        long startTime=System.currentTimeMillis();
        TraceContext.setStartTimestamp(String.valueOf(startTime));
    }
    public static void afterInvoke(String serviceName){
        long endTime=System.currentTimeMillis();
        long startTime=Long.valueOf(TraceContext.getStartTimestamp());
        long duration=endTime-startTime;

        ZipkinReporter.reportSpan(TraceContext.getTraceId(),TraceContext.getSpanId(),"server"+serviceName, TraceContext.getParentSpanId(),serviceName,"server",startTime,duration);
        TraceContext.clear();
    }
}
