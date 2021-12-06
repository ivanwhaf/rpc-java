package com.company.rpcclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;


public class RpcNioClient {

    public static Object getRemoteProxy(final Class<?> serviceInterface, final InetSocketAddress addr) {
        return Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Socket socket = new Socket();
                socket.connect(addr);

                OutputStream output = socket.getOutputStream();
                //output.writeUTF(serviceInterface.getName());
                //output.writeUTF(method.getName());

                JSONObject json = new JSONObject();
                json.put("serviceName", serviceInterface.getName());
                json.put("methodName", method.getName());

                String parameterTypesJson = JSON.toJSONString(method.getParameterTypes());
                String argsJson = JSON.toJSONString(args);

                json.put("parameterTypes", parameterTypesJson);
                json.put("args", argsJson);

                //request method
                output.write(json.toString().getBytes());
                System.out.println("[Client]Request json: " + json);

                //read result
                byte[] bytes = new byte[4096];
                InputStream input = socket.getInputStream();

                int length = input.read(bytes);

                String res = new String(bytes, 0, length);
                json = JSONObject.parseObject(res);
                System.out.println("[Client]Result json: " + json);
                System.out.println("-------------------------------------------------------------");
                return JSON.parseObject(json.getString("result"), Object.class);
            }
        });
    }
}
