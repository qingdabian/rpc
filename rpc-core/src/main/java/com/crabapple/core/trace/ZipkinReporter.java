package com.crabapple.core.trace;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Slf4j
public class ZipkinReporter {
    private static final String ZIPKIN_URL="http://localhost:9411";
    private static final AsyncReporter<Span> reporter;
    //Zipkin上报器初始化
    static{
        OkHttpSender sender=OkHttpSender.create(ZIPKIN_URL);
        reporter=AsyncReporter.create(sender);
    }

    public static void reportSpan(String traceId,String spanId,String name,String parentId,String serviceName,String type,long startTime,long duration){
        Span span=Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .name(name)
                .parentId(parentId)
                .name(serviceName)
                .timestamp(startTime*1000)
                .duration(duration*1000)
                .putTag("type",type)
                .putTag("serviceName",serviceName)
                .build();
        reporter.report(span);
        log.info("当前上报的日志是:{}",traceId);
    }

    public static void close(){
        reporter.close();
    }
}
