package com.company.rpcserver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.company.service.Calculator;
import com.company.service.ICalculator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcNioServer {
    private static final int port = 8888;
    private static final Map<String, Class> registry = new HashMap<>();
    private final ExecutorService pool = Executors.newFixedThreadPool(128);

    public void register(String name, Class clazz) {
        registry.put(name, clazz);
        System.out.println("Register service:" + name);
    }

    public void stop() {
        pool.shutdown();
    }

    public void start() {
        // register class
        register(ICalculator.class.getName(), Calculator.class);

        ServerSocketChannel server = null;
        Selector selector = null;
        try {
            server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress("localhost", port));
            server.configureBlocking(false);
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.socket().setReuseAddress(true);
                        sc.register(selector, SelectionKey.OP_READ);
                    }

                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        //read buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        StringBuilder s = new StringBuilder();
                        int length = sc.read(buffer);
                        while (length > 0) {
                            s.append(new String(buffer.array(), 0, length));
                            buffer.clear();
                            length=sc.read(buffer);
                        }

                        //json data
                        JSONObject json = JSONObject.parseObject(s.toString());
                        //System.out.println("[Server]Receive json: " + json);

                        String serviceName = json.getString("serviceName");
                        System.out.println("[Server]service: " + serviceName);
                        String methodName = json.getString("methodName");
                        System.out.println("[Server]method: " + methodName);

                        //convert data type
                        List<Class> lst = JSON.parseArray(json.getString("parameterTypes"), Class.class);
                        Class<?>[] parameterTypes = new Class<?>[lst.size()];
                        for (int i = 0; i < lst.size(); i++) parameterTypes[i] = lst.get(i);
                        System.out.println("[Server]parameterTypes: " + Arrays.toString(parameterTypes));

                        List<Object> lst2 = JSON.parseArray(json.getString("args"), Object.class);
                        Object[] args = new Object[lst2.size()];
                        for (int i = 0; i < lst2.size(); i++) args[i] = lst2.get(i);
                        System.out.println("[Server]args: " + Arrays.toString(args));

                        //run method
                        Class serviceClass = registry.get(serviceName);
                        Method method = serviceClass.getMethod(methodName, parameterTypes);
                        Object result = method.invoke(serviceClass.newInstance(), args);

                        //send result back to client
                        json = new JSONObject();
                        json.put("method", methodName);
                        json.put("args",Arrays.toString(args));
                        json.put("result", JSON.toJSONString(result));
                        ByteBuffer sendBuffer = ByteBuffer.wrap(json.toString().getBytes());
                        sc.write(sendBuffer);
                    }
                }

            } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }

        }
    }
}
