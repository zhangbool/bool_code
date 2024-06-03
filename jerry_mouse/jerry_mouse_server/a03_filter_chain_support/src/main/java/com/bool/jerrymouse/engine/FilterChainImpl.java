package com.bool.jerrymouse.engine;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * @author : 不二
 * @date : 2024/6/1-20:47
 * @desc :
 **/
public class FilterChainImpl  implements FilterChain {

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

            System.out.println("--------------test--------------");
            servlet.service(request, response);
        }
    }

}
