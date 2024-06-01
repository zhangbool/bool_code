package com.bool.jerrymouse.engine.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;


import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/*")
public class LogFilter implements Filter {

    Logger logger = Logger.getLogger(LogFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        logger.info(req.getMethod() + ":" + req.getRequestURI());
        chain.doFilter(request, response);
    }
}
