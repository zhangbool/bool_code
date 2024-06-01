package com.bool.jerrymouse.connector;

import com.bool.jerrymouse.engine.HttpServletRequestImpl;
import com.bool.jerrymouse.engine.HttpServletResponseImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

/**
 * @author : 不二
 * @date : 2024/5/29-15:13
 * @desc :
 **/
public class HttpConnector implements HttpHandler, AutoCloseable  {

    final HttpServer httpServer;
    final String host;
    final int port;


    public HttpConnector(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    public void start() {
        if (this.httpServer != null) {
            this.httpServer.start();
            // logger.info("start jerrymouse http server at {}:{}", host, port);
            System.out.println("start jerrymouse http server at +" + host + ":" + port);
        }
    }

    @Override
    public void close() {
        this.httpServer.stop(3);
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("处理请求.....");
        try {
            var adapter = new HttpExchangeAdapter(exchange);
            var request = new HttpServletRequestImpl(adapter);
            var response = new HttpServletResponseImpl(adapter);
            process(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 这里原先方法错误的写成了: HttpServletRequestImpl, HttpServletResponseImpl结果根本不对,
    // 上面45行: var request = new HttpServletRequestImpl(adapter); 既不报错, 也走不进去??? 啥原因..... 郁闷....
    // 等会, 又行了???? 不明所以......
//    protected void process(HttpServletRequestImpl request, HttpServletResponseImpl response) throws Exception {
    protected void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("process...");
        String name = request.getParameter("name");
        String html = "<h1>Hello, " + (name == null ? "world" : name) + ".</h1>";
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.write(html);
        pw.close();
    }


}
