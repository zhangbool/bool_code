package com.bool.jerrymouse.engine.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/hello")
public class HelloFilter implements Filter {

    Logger logger = Logger.getLogger(HelloFilter.class.getName());
    Set<String> names = Set.of("Bob", "Alice", "Tom", "Jerry");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String name = req.getParameter("name");
        logger.info("Check parameter name = " +  name);
        if (name != null && names.contains(name)) {
            chain.doFilter(request, response);
        } else {
            logger.warning("Access denied: name = " +  name);
            HttpServletResponse resp = (HttpServletResponse) response;
            if (!response.isCommitted()) {
                resp.resetBuffer(); // Clear any data in the buffer
                resp.sendError(403, "Forbidden");
            }
            return; // Ensure to return to stop further processing
        }
    }
}
