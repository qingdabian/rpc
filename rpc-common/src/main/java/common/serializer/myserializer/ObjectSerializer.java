package common.serializer.myserializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        byte[] bytes;
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos=new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            bytes=bos.toByteArray();
            oos.close();
            bos.close();
            return bytes;
        } catch (IOException e) {
            log.error("ObjectSerializer serialize error",e);
            return null;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, int typeclass) {
        ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
        try{
            ObjectInputStream ois=new ObjectInputStream(bis);
            Object object=ois.readObject();
            ois.close();
            bis.close();
            return object;
        } catch (IOException e) {
            log.error("ObjectSerializer deserialize error",e);
        } catch (ClassNotFoundException e) {
            log.error("ObjectSerializer deserialize error",e);
        }
        return null;
    }

    @Override
    public int getType() {
        return 0;
    }
}
