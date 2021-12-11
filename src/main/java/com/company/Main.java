package com.company;

import com.company.rpcclient.RpcNioClient;
import com.company.rpcserver.RpcNioServer;
import com.company.service.ICalculator;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {
        // server
        new Thread(new Runnable() {
            @Override
            public void run() {
                //RpcServer server = new RpcServer();
                RpcNioServer server=new RpcNioServer();
                server.start();
            }
        }).start();


        // Client
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            ICalculator calculator = (ICalculator) RpcNioClient.getRemoteProxy(ICalculator.class, new InetSocketAddress("127.0.0.1", 8888));
            try {
                calculator.add(i,1);
                //calculator.add(i,2);
            } catch (Exception e) {
                System.out.println("Call remote server failed!");
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start + "ms");

    }
}
