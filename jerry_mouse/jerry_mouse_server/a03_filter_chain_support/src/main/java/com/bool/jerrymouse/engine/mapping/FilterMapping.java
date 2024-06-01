package com.bool.jerrymouse.engine.mapping;

import jakarta.servlet.Filter;

import java.util.regex.Pattern;

/**
 * @author : 不二
 * @date : 2024/5/31-18:18
 * @desc : 过滤器的mapping
 **/
public class FilterMapping extends AbstractMapping {

    public final Filter filter;

    public FilterMapping(String urlPattern, Filter filter) {
        super(urlPattern);
        this.filter = filter;
    }
}
