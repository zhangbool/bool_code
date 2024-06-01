package com.bool.jerrymouse.utils;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author : 不二
 * @date : 2024/5/30-17:52
 * @desc :
 **/
public class AnnoUtils {

    static Logger logger = Logger.getLogger(AnnoUtils.class.getName());

    public static String getServletName(Class<? extends Servlet> clazz) {
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w != null && !w.name().isEmpty()) {
            return w.name();
        }
        return defaultNameByClass(clazz);
    }

    public static String getFilterName(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w != null && !w.filterName().isEmpty()) {
            return w.filterName();
        }
        return defaultNameByClass(clazz);
    }

    public static Map<String, String> getServletInitParams(Class<? extends Servlet> clazz) {
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w == null) {
            return Map.of();
        }
        // 获取servlet预制的参数
        return initParamsToMap(w.initParams());
    }

    public static Map<String, String> getFilterInitParams(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return Map.of();
        }
        return initParamsToMap(w.initParams());
    }

    public static String[] getServletUrlPatterns(Class<? extends Servlet> clazz) {
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w == null) {
            return new String[0];
        }

        // w.value() 和 w.urlPatterns() 为啥都是: the URL patterns of the servlet??????
        logger.info("w.value()是: " + w.value());
        logger.info("w.urlPatterns()是: " + w.urlPatterns());
        return arraysToSet(w.value(), w.urlPatterns()).toArray(String[]::new);
    }

    public static String[] getFilterUrlPatterns(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return new String[0];
        }
        return arraysToSet(w.value(), w.urlPatterns()).toArray(String[]::new);
    }

    public static EnumSet<DispatcherType> getFilterDispatcherTypes(Class<? extends Filter> clazz) {
        WebFilter w = clazz.getAnnotation(WebFilter.class);
        if (w == null) {
            return EnumSet.of(DispatcherType.REQUEST);
        }
        return EnumSet.copyOf(Arrays.asList(w.dispatcherTypes()));
    }

    private static String defaultNameByClass(Class<?> clazz) {
        String name = clazz.getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    private static Map<String, String> initParamsToMap(WebInitParam[] params) {
        return Arrays.stream(params).collect(Collectors.toMap(p -> p.name(), p -> p.value()));
    }

    private static Set<String> arraysToSet(String[] arr1) {
        Set<String> set = new LinkedHashSet<>();
        for (String s : arr1) {
            set.add(s);
        }
        return set;
    }

    // 两个放一起去重...
    private static Set<String> arraysToSet(String[] arr1, String[] arr2) {
        Set<String> set = arraysToSet(arr1);
        set.addAll(arraysToSet(arr2));
        return set;
    }
}
