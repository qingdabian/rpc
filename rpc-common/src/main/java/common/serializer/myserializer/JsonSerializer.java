package common.serializer.myserializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSerializer implements Serializer {
    public byte[] serialize(Object object) {
        byte[] bytes= JSON.toJSONBytes(object, JSONWriter.Feature.PrettyFormat);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int typeclass) {
        Object object=null;
        switch(typeclass){
            case 0:
                RpcRequest request=JSON.parseObject(bytes,RpcRequest.class, JSONReader.Feature.SupportClassForName);
                Object[] objects=new Object[request.getParamTypes().length];
                for(int i=0;i<objects.length;i++){
                    Class<?> clazz=request.getParamTypes()[i];
                    if(!clazz.isAssignableFrom(request.getParams()[i].getClass())){
                        objects[i]=JSON.toJavaObject((JSONObject)request.getParams()[i],request.getParamTypes()[i]);
                    }else{
                        objects[i]=request.getParams()[i];
                    }
                }
                request.setParams(objects);
                object=request;
                break;
            case 1:
                RpcResponse response=JSON.parseObject(bytes,RpcResponse.class,JSONReader.Feature.SupportClassForName);
                log.error("响应序列化时的内容：{}",response);
                Class<?> dataType=response.getDataType();
                if(!dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSON.toJavaObject((JSONObject)response.getData(),dataType));
                }
                object=response;
                break;
            default:
                log.error("typeclass not found");
                break;
        }
        return object;
    }

    @Override
    public int getType() {
        return 1;
    }
}
