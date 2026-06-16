package common.util;

import cn.hutool.setting.dialect.Props;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigUtil {
    public static <T> T loadConfig(Class<T> targetClass,String prefix){
        return loadConfig(targetClass,prefix,"");
    }
    public static <T> T loadConfig(Class<T> targetClass,String prefix,String environment){
        StringBuilder config=new StringBuilder("application");
        if(!StringUtil.isNullOrEmpty(environment)){
            config.append("-").append(environment);
        }
        config.append(".properties");
        Props props=new Props(config.toString());
        if(props.isEmpty()){
            log.warn("config file {} is empty",config.toString());
        }else{
            log.info("config file {} is loaded",config.toString());
        }
        try{
            return props.toBean(targetClass);
        }catch (Exception e){
            log.error("配置转换失败，目标类{}",targetClass);
            throw new RuntimeException("配置加载失败",e);
        }
    }
}
