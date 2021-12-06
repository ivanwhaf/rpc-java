package com.company;

import com.company.rpcclient.RpcClient;
import com.company.rpcserver.RpcServer;
import com.company.service.ICalculator;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        // server
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
            try {
                System.out.println(calculator.add(i, 1));
            } catch (Exception e) {
                System.out.println("Connect failed!");
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start + "ms");

    }
}
