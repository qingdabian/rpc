package common.serializer.myserializer;

import common.serializer.myserializer.JsonSerializer;
import common.serializer.myserializer.ObjectSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public interface Serializer {
    byte[] serialize(Object object);
    Object deserialize(byte[] bytes,int typeclass);

    int getType();

    static Serializer getSerializer(int serializerType){
        if(serializerType<0||serializerType>4){
            System.out.println("serializerType错误，必须在0-4之间");
            return null;
        }
        Map<Integer,Serializer> serializers=new ConcurrentHashMap<>();
        serializers.put(0,new ObjectSerializer());
        serializers.put(1,new JsonSerializer());
        serializers.put(2,new HessianSerializer());
        serializers.put(3,new ProtostuffSerializer());
        serializers.put(4,new HessianSerializer());
        return serializers.get(serializerType);
    }
}
