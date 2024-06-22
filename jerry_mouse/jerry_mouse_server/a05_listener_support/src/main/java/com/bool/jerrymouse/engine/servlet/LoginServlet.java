package com.bool.jerrymouse.engine.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author : 不二
 * @date : 2024/6/3-12:02
 * @desc :
 **/
@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    Map<String, String> users = Map.of( // user database
            "bob", "bob123", //
            "alice", "alice123", //
            "root", "admin123" //
    );

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        logger.info("username:{}, password:{}", username, password);

        String expectedPassword = users.get(username.toLowerCase());
        logger.info("username:{}, password:{}---expectedPassword:{}", username, password, expectedPassword);

        if (expectedPassword == null || !expectedPassword.equals(password)) {
            PrintWriter pw = resp.getWriter();
            pw.write("""
                    <h1>Login Failed</h1>
                    <p>Invalid username or password.</p>
                    <p><a href="/">Try again</a></p>
                    """);
            pw.close();
        } else {
            logger.info("login success------");
            // 在请求中拿到session, 设置相关属性
            req.getSession().setAttribute("username", username);

            logger.info("-------------开始进行页面跳转-------------");

            // 跳转到首页的页面
            // #todo: 这里目前应该还是有问题到!!!
            resp.sendRedirect("/");
        }
    }
}
