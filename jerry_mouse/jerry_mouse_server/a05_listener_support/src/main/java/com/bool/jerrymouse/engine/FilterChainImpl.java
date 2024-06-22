package com.bool.jerrymouse.engine;

import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author : 不二
 * @date : 2024/6/1-20:47
 * @desc :
 **/
public class FilterChainImpl  implements FilterChain {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    final Filter[] filters;
    final Servlet servlet;
    final int total;
    int index = 0;

    public FilterChainImpl(Filter[] filters, Servlet servlet) {
        this.filters = filters;
        this.servlet = servlet;
        this.total = filters.length;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (index < total) {
            int current = index;
            index++;
            filters[current].doFilter(request, response, this);
        } else {
            logger.info("filter已处理完毕, 开始处理servlet...");
            servlet.service(request, response);
        }
    }

}
