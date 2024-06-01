package com.bool.jerrymouse.connector;

import com.bool.jerrymouse.engine.HttpServletRequestImpl;
import com.bool.jerrymouse.engine.HttpServletResponseImpl;
import com.bool.jerrymouse.engine.ServletContextImpl;
import com.bool.jerrymouse.engine.filter.HelloFilter;
import com.bool.jerrymouse.engine.filter.LogFilter;
import com.bool.jerrymouse.engine.servlet.HelloServlet;
import com.bool.jerrymouse.engine.servlet.IndexServlet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author : 不二
 * @date : 2024/5/29-15:13
 * @desc : 连接器 -> 上下文 -> 请求 -> response
 **/
public class HttpConnector implements HttpHandler, AutoCloseable  {

    Logger logger = Logger.getGlobal();

    final HttpServer httpServer;
    final String host;
    final int port;

    final ServletContextImpl servletContext;

    public HttpConnector(String host, int port) throws IOException {

        this.servletContext = new ServletContextImpl();
        // #todo: 这里还有个初始化方法, 暂时不知道有啥必要性不, 先不写
        // this.servletContext.initialize(List.of(IndexServlet.class, HelloServlet.class));

        this.servletContext.initFilters(List.of(LogFilter.class, HelloFilter.class));
        this.servletContext.initServlets(List.of(IndexServlet.class, HelloServlet.class));


        this.host = host;
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    public void start() {
        if (this.httpServer != null) {
            this.httpServer.start();
            // logger.info("start jerrymouse http server at {}:{}", host, port);
            logger.info("start jerrymouse http server at +" + host + ":" + port);
        }
    }

    @Override
    public void close() {
        this.httpServer.stop(3);
    }

    @Override
    public void handle(HttpExchange exchange) {
        logger.info("处理请求.....");
        try {
            var adapter = new HttpExchangeAdapter(exchange);
            var request = new HttpServletRequestImpl(adapter);
            var response = new HttpServletResponseImpl(adapter);

            // 之前是来了请求直接统一处理
            // process(request, response);

            // 我们这里要通过上下文来处理不同的路径请求
            // process:
            try {
                this.servletContext.process(request, response);
                logger.info("==============================done==============================");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {

            logger.info(e.getMessage());

            throw new RuntimeException(e);
        }
    }

    // 这里是所有的请求都走这里. 但是一个庞大的项目, 肯定是要进行请求状态存储的. 使用servlet进行处理. 再各自的servlet里面进行请求处理
    // 这里原先方法错误的写成了: HttpServletRequestImpl, HttpServletResponseImpl结果根本不对,
    // 上面45行: var request = new HttpServletRequestImpl(adapter); 既不报错, 也走不进去??? 啥原因..... 郁闷....
    // 等会, 又行了???? 不明所以......
//    protected void process(HttpServletRequestImpl request, HttpServletResponseImpl response) throws Exception {
    protected void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        System.out.println("process...");
        logger.info("process...");
        String name = request.getParameter("name");
        String html = "<h1>Hello, " + (name == null ? "world" : name) + ".</h1>";
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        pw.write(html);
        pw.close();
    }
}
