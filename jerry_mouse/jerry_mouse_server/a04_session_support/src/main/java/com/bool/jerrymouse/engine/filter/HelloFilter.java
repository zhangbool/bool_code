package com.bool.jerrymouse.engine.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns = "/hello")
public class HelloFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Set<String> names = Set.of("Bob", "Alice", "Tom", "Jerry");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String name = req.getParameter("name");
        logger.info("Check parameter name = " +  name);
        try{
            if (name != null && names.contains(name)) {
                chain.doFilter(request, response);
            } else {
                logger.info("Access denied: name = {}", name);
                HttpServletResponse resp = (HttpServletResponse) response;

                // 注意: 之前这里只是设置了sendError, 没有write内容, 请求就会一直挂着. 暂时还没搞明白为啥原先的代码可以返回
                // 老子终于明白了: 这个sendError是覆写的方法!!!!!!!草
                resp.sendError(403, "Forbidden");
                /*try (PrintWriter out = resp.getWriter()) {
                    out.write("Forbidden");
                }*/
            }
        } catch (Exception e) {
            logger.error("Exception in filter: {}", e.getMessage());
            HttpServletResponse resp = (HttpServletResponse) response;
            if (!resp.isCommitted()) {
                resp.resetBuffer();
                resp.sendError(500, "Internal Server Error");
                /*String s = "<h1>Hello, world.</h1><p>" + LocalDateTime.now().withNano(0) + "</p>";
                try (OutputStream out = resp.getOutputStream()) {
                    out.write(s.getBytes(StandardCharsets.UTF_8));
                }*/
            }
        }
    }
}
