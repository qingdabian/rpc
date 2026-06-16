package com.crabapple.core.client.rpcclient.impl;



import com.crabapple.core.client.rpcclient.RpcClient;
import com.crabapple.core.client.servicecenter.ServiceCenter;
import com.crabapple.core.client.servicecenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SimpleRpcClient implements RpcClient {
    private String host;
    private int port;
    private ServiceCenter serviceCenter;
    public SimpleRpcClient(){
        this.serviceCenter=new ZKServiceCenter();
    }
//    public SimpleRpcClient(String host,int port){
//        this.host=host;
//        this.port=port;
//    }
    @Override
    public RpcResponse sendRpcRequest(RpcRequest request) {
        try{
            InetSocketAddress addr=serviceCenter.serviceDicovery(request.getInterfacename());
            Socket socket=new Socket(addr.getAddress(),addr.getPort());
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

    @Override
    public void close() {
        serviceCenter.close();
    }
}
