package com.company;

import com.company.rpcclient.RpcClient;
import com.company.rpcserver.RpcServer;
import com.company.service.ICalculator;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RpcServer server = new RpcServer();
                server.start();
            }
        }).start();


        // Client
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            ICalculator calculator = (ICalculator) RpcClient.getRemoteProxy(ICalculator.class, new InetSocketAddress("127.0.0.1", 8888));
            //System.out.println(calculator.add(1,2));
            try {
                calculator.add(i, 1);
            } catch (Exception ignored) {
                System.out.println("Connect failed!");
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start + "ms");

    }
}
