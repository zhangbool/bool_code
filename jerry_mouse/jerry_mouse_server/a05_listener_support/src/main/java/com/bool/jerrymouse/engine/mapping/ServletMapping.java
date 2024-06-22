package com.bool.jerrymouse.engine.mapping;

import jakarta.servlet.Servlet;

/**
 * @author : 不二
 * @date : 2024/5/30-15:27
 * @desc : servlet的映射
 **/
public class ServletMapping extends AbstractMapping {

    public final Servlet servlet;

    public ServletMapping(String urlPattern, Servlet servlet) {
        // #todo: 这里具体逻辑, 需要进一步看下
        super(urlPattern);
        this.servlet = servlet;
    }

}
