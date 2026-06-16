package com.crabapple.core.config;

import com.crabapple.core.client.servicecenter.balance.ConsistencyHashBalance;
import com.crabapple.core.server.serviceregister.ZKServiceRegister;
import common.serializer.myserializer.Serializer;
import lombok.*;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RpcConfig {
    private String name="krpc";
    private Integer port=9999;
    private String host="localhost";
    private String version="1.0.0";
    private String registry=new ZKServiceRegister().toString();
    private String serializer= Serializer.getSerializer(0).toString();
    private String loadBalance=new ConsistencyHashBalance().toString();
}
