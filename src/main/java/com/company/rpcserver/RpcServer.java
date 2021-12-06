package com.company.rpcserver;

import com.company.service.Calculator;
import com.company.service.ICalculator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
                //BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String serviceName = input.readUTF();
                System.out.println("Client service name:" + serviceName);
                String methodName = input.readUTF();
                System.out.println("Client method:" + methodName);
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                //System.out.println("Client:" + parameterTypes);
                Object[] args = (Object[]) input.readObject();
                //System.out.println("Client:" + args);

                Class serviceClass = registry.get(serviceName);
                Method method = serviceClass.getMethod(methodName, parameterTypes);

                Object result = method.invoke(serviceClass.newInstance(), args);

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(result);
                System.out.println(result);
            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
