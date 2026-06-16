package com.crabapple.core;

import com.crabapple.core.config.RpcConfig;
import com.crabapple.core.config.RpcConstant;
import common.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;
    public static void init(RpcConfig rpcConfig){
        RpcApplication.rpcConfig=rpcConfig;
        log.info("rpc config is loaded",rpcConfig);
    }

    public static void init(){
        RpcConfig customRpcConfig;
        try{
            customRpcConfig= ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.CONFIG_FILE_PREFIX);
            log.info("custom rpc config is loaded",customRpcConfig);
        }catch (Exception e){
            customRpcConfig=new RpcConfig();
            log.error("custom rpc config load failed",e);
        }
        init(customRpcConfig);
    }
    public static RpcConfig getRpcConfig(){
        if(rpcConfig==null){
            synchronized (RpcApplication.class){
                if(rpcConfig==null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
