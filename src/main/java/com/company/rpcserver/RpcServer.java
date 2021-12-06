package com.company.rpcserver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.company.service.Calculator;
import com.company.service.ICalculator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcServer {

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

        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Start RPC Server");

        while (true) {
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(socket.getPort());
            pool.execute(new Handler(socket));
        }
    }

    private static class Handler implements Runnable {
        Socket socket = null;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Client " + socket.getInetAddress().getHostAddress() + " connect to server");
            try {
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                String s = (String) input.readObject();
                System.out.println(s);
                JSONObject json = JSONObject.parseObject(s);

                String serviceName = json.getString("serviceName");
                System.out.println("Client service name: " + serviceName);
                String methodName = json.getString("methodName");
                System.out.println("Client method: " + methodName);

                List<Class> lst = JSON.parseArray(json.getString("parameterTypes"), Class.class);
                Class<?>[] parameterTypes = new Class<?>[lst.size()];
                for (int i = 0; i < lst.size(); i++) parameterTypes[i] = lst.get(i);
                System.out.println("Client parameterTypes: " + Arrays.toString(parameterTypes));

                List<Object> lst2 = JSON.parseArray(json.getString("args"), Object.class);
                Object[] args = new Object[lst2.size()];
                for (int i = 0; i < lst2.size(); i++) args[i] = lst2.get(i);
                System.out.println("Client args: " + Arrays.toString(args));

                Class serviceClass = registry.get(serviceName);

                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), args);

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(result);
                System.out.println(result);
            } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
