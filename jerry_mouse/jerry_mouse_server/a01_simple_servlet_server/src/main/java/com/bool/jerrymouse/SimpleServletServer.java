package com.bool.jerrymouse;


import com.bool.jerrymouse.connector.HttpConnector;

/**
 * @author : 不二
 * @date : 2024/5/29-20:59
 * @desc :
 **/
public class SimpleServletServer {
    public static void main(String[] args) throws Exception {
        try (HttpConnector httpConnector = new HttpConnector("127.0.0.1", 8081)) {
            httpConnector.start();
            for (; ; ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("jerrymouse http server was shutdown.");
        }
    }
}
