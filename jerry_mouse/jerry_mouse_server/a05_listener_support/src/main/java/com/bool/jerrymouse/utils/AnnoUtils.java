package com.bool.jerrymouse.utils;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : 不二
 * @date : 2024/5/30-17:52
 * @desc :
 **/
public class AnnoUtils {

    static Logger logger = LoggerFactory.getLogger(AnnoUtils.class);
    /**
     * 根据Servlet获取对应的名字, string,
     * @param clazz
     * @return
     */
    public static String getServletName(Class<? extends Servlet> clazz) {
        WebServlet w = clazz.getAnnotation(WebServlet.class);
        if (w != null && !w.name().isEmpty()) {
            logger.info("从annotation中获取:name = {}", w.name());
            // 如果有name的标注, 使用name
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
        // 把类名首字母变小写
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        logger.info("annotation中无name, 获取默认值: name = {}", name);
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

    private static Set<String> arraysToSet(String[] arr1, String[] arr2) {
        Set<String> set = arraysToSet(arr1);
        set.addAll(arraysToSet(arr2));
        return set;
    }
}
