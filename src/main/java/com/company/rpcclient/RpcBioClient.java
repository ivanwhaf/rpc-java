package com.company.rpcclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;


public class RpcBioClient {

    public static Object getRemoteProxy(final Class<?> serviceInterface, final InetSocketAddress addr) {
        return Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Socket socket = new Socket();
                socket.connect(addr);

                JSONObject json = new JSONObject();
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                //output.writeUTF(serviceInterface.getName());
                //output.writeUTF(method.getName());

                json.put("serviceName", serviceInterface.getName());
                json.put("methodName", method.getName());

                String parameterTypesJson = JSON.toJSONString(method.getParameterTypes());
                String argsJson = JSON.toJSONString(args);

                json.put("parameterTypes", parameterTypesJson);
                json.put("args", argsJson);

                //output.writeObject(types);
                //output.writeObject(args);
                output.writeObject(json.toJSONString());
                System.out.println("[Client]Request json: "+json);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                Object res=input.readObject();
                System.out.println("[Client]Result json: " + res);
                System.out.println("------------------------------------------------");
                return res;
            }
        });
    }
}