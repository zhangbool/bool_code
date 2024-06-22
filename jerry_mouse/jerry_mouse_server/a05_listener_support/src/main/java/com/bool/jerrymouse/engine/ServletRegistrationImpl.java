package com.bool.jerrymouse.engine;

import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author : 不二
 * @date : 2024/5/30-17:01
 * @desc : 这个类似是servlet的一个外层封装
 **/
public class ServletRegistrationImpl implements ServletRegistration.Dynamic {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    final ServletContext servletContext;
    final Servlet servlet;
    final String name;
     final List<String> urlPatterns = new ArrayList<>(4);

    boolean initialized = false;

    public ServletRegistrationImpl(ServletContext servletContext, String name, Servlet servlet) {
        this.servletContext = servletContext;
        this.servlet = servlet;
        this.name = name;
        logger.info("当前的name是: " + name);
    }

    public ServletConfig getServletConfig() {

        // 这里为啥要自己记录呢, 这里不是记录, 而是为了先保存下来, 然后往servlet里面设置的.
        // 其实这里没有也行. 没有的话, 这几个属性就需要设置的时候直接取也行.
        // 感觉他这个代码写复杂了
        return new ServletConfig() {
            @Override
            public String getServletName() {
                return ServletRegistrationImpl.this.name;
            }
            @Override
            public ServletContext getServletContext() {
                return ServletRegistrationImpl.this.servletContext;
            }
            @Override
            public String getInitParameter(String name) {
                return null;
            }
            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
    }

    @Override
    public String getName() {
        return this.servlet.getServletConfig().getServletName();
    }

    @Override
    public String getClassName() {
        return this.servlet.getClass().getName();
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {

        // 这里先注视掉, 不太清楚是干啥的
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException("Missing urlPatterns.");
        }
        this.urlPatterns.addAll(Arrays.asList(urlPatterns));
        return Set.of();
    }

    @Override
    public Collection<String> getMappings() {
        return this.urlPatterns;
    }


    @Override
    public void setLoadOnStartup(int i) {
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement servletSecurityElement) {
        return Set.of();
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfigElement) {
    }

    @Override
    public void setRunAsRole(String s) {
    }

    @Override
    public void setAsyncSupported(boolean b) {
    }

    @Override
    public String getRunAsRole() {
        return "";
    }


    @Override
    public boolean setInitParameter(String s, String s1) {
        return false;
    }

    @Override
    public String getInitParameter(String s) {
        return "";
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> map) {
        return Set.of();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return Map.of();
    }
}
