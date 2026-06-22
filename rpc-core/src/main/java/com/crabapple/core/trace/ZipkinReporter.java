package com.crabapple.core.trace;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Slf4j
public class ZipkinReporter {
    private static final String ZIPKIN_URL="http://localhost:9411/api/v2/spans";
    private static final AsyncReporter<Span> reporter;
    //Zipkin上报器初始化
    static{
        OkHttpSender sender=OkHttpSender.create(ZIPKIN_URL);
        reporter=AsyncReporter.create(sender);
        System.out.println("建立了和zipkin的连接");
    }

    public static void reportSpan(String traceId,String spanId,String name,String parentId,String serviceName,String type,long startTime,long duration){
        Span span=Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .name(name)
                .parentId(parentId)
                .timestamp(startTime*1000)
                .duration(duration*1000)
                .putTag("type",type)
                .putTag("serviceName",serviceName)
                .localEndpoint(Endpoint.newBuilder().serviceName(serviceName).build())
                .build();
        reporter.report(span);
        reporter.flush();
//        try{
//            Thread.sleep(3000);
//        }catch(Exception e){
//            log.error("发送链路信息等待时发生异常:{ }",e);
//        }
        System.out.println("服务端当前上报的日志是:  "+span);
        log.info("服务端当前上报的日志是:{}",traceId);
    }
    public static void reportClientSpan(String traceId,String spanId,String name,String parentId,String serviceName,String type,long startTime,long duration){
        Span span=Span.newBuilder()
                .traceId(traceId)
                .id(spanId)
                .name(name)
                .timestamp(startTime*1000)
                .duration(duration*1000)
                .putTag("type",type)
                .putTag("serviceName",serviceName)
                .localEndpoint(Endpoint.newBuilder().serviceName(serviceName).build())
                .build();
        reporter.report(span);
//        try{
//            Thread.sleep(3000);
//        }catch(Exception e){
//            log.error("发送链路信息等待时发生异常:{ }",e);
//        }
        reporter.flush();
        System.out.println("客户端端当前上报的日志是:  "+span);
        log.info("当前上报的日志是:{}",traceId);
    }

    public static void close(){
        reporter.close();
    }
}
