package com.crabapple.core.trace;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

import java.util.UUID;

public class TraceIdGenerator {
    private static final SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0L);
    public static String generateTraceId() {
        return Long.toHexString(generator.nextId());
    }
    // 生成UUID作为traceId
    public static String generateTraceIdUUID(){
        UUID uuid= UUID.randomUUID();
        String uuidstring=uuid.toString();
        String uuidWithoutHyphen=uuidstring.replace("-","");
        return uuidWithoutHyphen;
    }
    public static String generateSpanId(){
        return String.valueOf(System.currentTimeMillis());
    }
    // 生成雪花ID作为traceId
    static class SnowflakeIdGenerator{
        private final long workerId;
        private final long epoch=1694502400000L;
        private long sequence=0L;
        private long lastTimestamp=-1L;
        public SnowflakeIdGenerator(long workerId){
            if(workerId<0||workerId>1023) {
                throw new IllegalArgumentException("workerId must be between 0 and 1023");
            }
            this.workerId=workerId;
        }
        public synchronized long nextId(){
            long timestamp=System.currentTimeMillis();
            if(timestamp<lastTimestamp){
                throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            }
            if(timestamp==lastTimestamp){
                sequence=(sequence+1L)&0xFFF;
                // 序列号溢出，等待下毫秒
                if(sequence==0L){
                    timestamp=waitNextMillisecond(lastTimestamp);
                }
            }else{
                sequence=0L;
            }
            lastTimestamp=timestamp;
            return (timestamp-epoch)<<22|workerId<<12|sequence;
        }
        private long waitNextMillisecond(long lastTimestamp){
            long timestamp=System.currentTimeMillis();
            while(timestamp<=lastTimestamp){
                timestamp=System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}
