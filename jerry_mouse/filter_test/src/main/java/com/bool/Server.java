package com.bool;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author : 不二
 * @date : 2024/5/29-14:03
 * @desc : 手写tomcat第一步: 实现http服务器: 基于HttpHandler实现的最简单的服务器, 不涉及socket
 * http -> tcp
 * 通过继承: HttpHandler, 可以直接实现一个网络服务器, 而不需要再进行socket变成
 **/
public class Server implements HttpHandler, AutoCloseable {

     final Logger logger = LoggerFactory.getLogger(getClass());
    public static void main(String[] args)  {
        String host = "127.0.0.1";
        int port = 8080;

        try(Server connector = new Server(host, port)){
            connector.start();
            for (;;) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    final HttpServer httpServer;
    final String host;
    final int port;

    /**
     * 这里是服务器的连接器: 也就是connector
     * @param host
     * @param port
     * @throws IOException
     */
    public Server(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/01", this);
    }

    public void start() {
        if (this.httpServer != null) {
            this.httpServer.start();
            logger.info("start jerrymouse http server at +{}:{}", host, port);
        }
    }

    @Override
    public void close() throws Exception {
        this.httpServer.stop(3);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // 获取请求方法、URI、Path、Query等:
        String method = httpExchange.getRequestMethod();
        URI uri = httpExchange.getRequestURI();
        String path = uri.getPath();
        String query = uri.getRawQuery();
        logger.info("method: {}, path: {}, query: {}", method, path, query); ;

        // 输出响应的Header:
        Headers respHeaders = httpExchange.getResponseHeaders();
        respHeaders.set("Content-Type", "text/html; charset=utf-8");
        respHeaders.set("Cache-Control", "no-cache");
        // 设置200响应:
        httpExchange.sendResponseHeaders(200, 0);
        String s = "<h1>Hello, world.</h1><p>" + LocalDateTime.now().withNano(0) + "</p>";
        try (OutputStream out = httpExchange.getResponseBody()) {
            out.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }

}
