package com.bool.jerrymouse;


import com.bool.jerrymouse.connector.HttpConnector;

import java.util.logging.Logger;

/**
 * @author : 不二
 * @date : 2024/5/29-20:59
 * @desc : 从HttpServer一路拓展, 先通过适配器模式, 把原先的: HttpExchange转换为HttpServletRequest和HttpServletResponse
 *         然后这里再增加上下文支持: 支持不同的路径适配
 **/
public class SimpleServletServer {

    public static void main(String[] args) throws Exception {

        Logger logger = Logger.getGlobal();

        try (HttpConnector httpConnector = new HttpConnector("127.0.0.1", 8083)) {
            httpConnector.start();
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            // System.out.println("jerrymouse http server was shutdown.");
            logger.info("jerrymouse http server was shutdown.");
        }
    }
}
