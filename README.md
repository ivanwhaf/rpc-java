# rpc-java
A simple java-rpc framework

## beta v0.1
BIO + Thread pool
1000req/800ms

## beta v0.2
BIO + Thread pool + Fastjson
1000req/630ms

## beta v0.3
NIO(No thread pool) + Fastjson
1000req/1000ms

## beta v0.3.1
Fix bug: when gc, SocketChannel.read() return -1