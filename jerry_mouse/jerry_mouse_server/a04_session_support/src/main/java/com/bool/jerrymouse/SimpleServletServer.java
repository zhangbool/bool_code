package com.bool.jerrymouse;


import com.bool.jerrymouse.connector.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : 不二
 * @date : 2024/5/29-20:59
 * @desc : 从HttpServer一路拓展, 先通过适配器模式, 把原先的: HttpExchange转换为HttpServletRequest和HttpServletResponse
 *         然后这里再增加上下文支持: 支持不同的路径适配
 **/
public class SimpleServletServer {

    public static void main(String[] args) throws Exception {

        final Logger logger = LoggerFactory.getLogger(SimpleServletServer.class);
        try (HttpConnector httpConnector = new HttpConnector("127.0.0.1", 8084)) {
            httpConnector.start();
            logger.info("tomcat server started successfully");
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            logger.info("jerrymouse http server was shutdown.");
        }
    }
}
