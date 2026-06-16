package com.crabapple.core.iocclient;



import common.message.RpcRequest;
import common.message.RpcResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    public static RpcResponse send(String host, int port, RpcRequest request){
        try{
            Socket socket=new Socket(host,port);
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());

            oos.writeObject(request);
            oos.flush();

            RpcResponse response=(RpcResponse)ois.readObject();
            return response;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
