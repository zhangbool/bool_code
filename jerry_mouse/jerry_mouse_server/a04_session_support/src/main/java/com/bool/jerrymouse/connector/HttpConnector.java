package com.bool.jerrymouse.connector;

import com.bool.jerrymouse.engine.HttpServletRequestImpl;
import com.bool.jerrymouse.engine.HttpServletResponseImpl;
import com.bool.jerrymouse.engine.ServletContextImpl;
import com.bool.jerrymouse.engine.filter.HelloFilter;
import com.bool.jerrymouse.engine.filter.LogFilter;
import com.bool.jerrymouse.engine.servlet.HelloServlet;
import com.bool.jerrymouse.engine.servlet.IndexServlet;
import com.bool.jerrymouse.engine.servlet.LoginServlet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author : 不二
 * @date : 2024/5/29-15:13
 * @desc : 连接器 -> 上下文 -> 请求 -> response
 **/
public class HttpConnector implements HttpHandler, AutoCloseable  {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    final HttpServer httpServer;
    final String host;
    final int port;

    final ServletContextImpl servletContext;

    public HttpConnector(String host, int port) throws IOException {

        this.servletContext = new ServletContextImpl();

        this.servletContext.initFilters(List.of(LogFilter.class, HelloFilter.class));
        this.servletContext.initServlets(List.of(IndexServlet.class, HelloServlet.class, LoginServlet.class));

        this.host = host;
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    public void start() {
        if (this.httpServer != null) {
            this.httpServer.start();
            logger.info("start jerrymouse http server at +{}:{}", host, port);
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
            var response = new HttpServletResponseImpl(adapter);

            // 为了支持session, 则在请求中需要: 引入servletContext和response. 前者用来获取sessionManager, 后者暂时不太明确...
            var request = new HttpServletRequestImpl(this.servletContext, adapter, response);

            try {
                this.servletContext.process(request, response);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
