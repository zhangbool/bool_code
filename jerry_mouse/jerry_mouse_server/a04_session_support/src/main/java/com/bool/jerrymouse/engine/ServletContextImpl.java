package com.bool.jerrymouse.engine;

import com.bool.jerrymouse.engine.mapping.FilterMapping;
import com.bool.jerrymouse.engine.mapping.ServletMapping;
import com.bool.jerrymouse.utils.AnnoUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author : 不二
 * @date : 2024/5/30-15:24
 * @desc : 上下文接口实现
 **/
public class ServletContextImpl implements ServletContext {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    SessionManager sessionManager = new SessionManager(this, 600);;

    final List<FilterMapping> filterMappings = new ArrayList<>();
    final Map<String, FilterRegistrationImpl> filterRegistrations = new HashMap<>();

    // 上下文中hold一个对象集合, 后续用来保持状态
    final List<ServletMapping> servletMappings = new ArrayList<>();
    private final Map<String, ServletRegistrationImpl> servletRegistrations = new HashMap<>();

    // 这个也不知道是干啥的
    // final Map<String, Filter> nameToFilters = new HashMap<>();
    public void initFilters(List<Class<?>> filterClasses) {
        for (Class<?> c : filterClasses) {
            WebFilter wf = c.getAnnotation(WebFilter.class);
            if (wf != null) {
                logger.info("auto register @WebFilter: " + c.getName());
                @SuppressWarnings("unchecked")
                Class<? extends Filter> clazz = (Class<? extends Filter>) c;
                FilterRegistration.Dynamic registration = this.addFilter(AnnoUtils.getFilterName(clazz), clazz);
                registration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, AnnoUtils.getFilterUrlPatterns(clazz));
                registration.setInitParameters(AnnoUtils.getFilterInitParams(clazz));
            }
        }

        // init filters:
        for (String name : this.filterRegistrations.keySet()) {
            var registration = this.filterRegistrations.get(name);
            try {
                registration.filter.init(registration.getFilterConfig());
                // this.nameToFilters.put(name, registration.filter);
                for (String urlPattern : registration.getUrlPatternMappings()) {
                    this.filterMappings.add(new FilterMapping(urlPattern, registration.filter));
                }
                registration.initialized = true;
            } catch (ServletException e) {
                logger.error("init filter failed: {} / {}{}", name, registration.filter.getClass().getName(), e.toString());
            }
        }
    }

    // 这里nameToServlets是干啥的? 这个目前是啥用没有
    // final Map<String, Servlet> nameToServlets = new HashMap<>();
    public void initServlets(List<Class<?>> servletClasses) {

        for (Class<?> c : servletClasses) {
            WebServlet ws = c.getAnnotation(WebServlet.class);
            if (ws != null) {
                logger.info("auto register @WebServlet: " +  c.getName());
                @SuppressWarnings("unchecked")
                Class<? extends Servlet> clazz = (Class<? extends Servlet>) c;

                // 在这里创建了一个局部registration, 所以这里干了啥?????
                // 好吧, 这里有个this.addServlet, 这里负责把当前的数据加入到servletRegistrations这个map中了
                ServletRegistration.Dynamic registration = this.addServlet(AnnoUtils.getServletName(clazz), clazz);

                // 这里先加进去, 后面还要取出来. 好像除了临时存一下之外, 没其他屁用了
                registration.addMapping(AnnoUtils.getServletUrlPatterns(clazz));
                registration.setInitParameters(AnnoUtils.getServletInitParams(clazz));
            }
        }

        // 从map中取出来, 进行相关设置
        // init servlets:
        for (String name : this.servletRegistrations.keySet()) {
            var registration = this.servletRegistrations.get(name);
            try {
                registration.servlet.init(registration.getServletConfig());
//                this.nameToServlets.put(name, registration.servlet);
                for (String urlPattern : registration.getMappings()) {
                    this.servletMappings.add(new ServletMapping(urlPattern, registration.servlet));
                }
                registration.initialized = true;
            } catch (ServletException e) {
                logger.error("init servlet failed: {} / {}: {}", name, registration.servlet.getClass().getName(), e.getMessage());
            }
        }

        // 这是干啥的呢
        // important: sort mappings:
        Collections.sort(this.servletMappings);
    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getRequestURI();

        logger.info("path:{}", path);
        if ("/favicon.ico".equals(path)) {
            System.out.println("-------------favicon.ico, 直接返回-------------");
            return;
        }

        // search servlet:
        Servlet servlet = null;
        for (ServletMapping mapping : this.servletMappings) {
            if (mapping.matches(path)) {
                servlet = mapping.servlet;
                break;
            }
        }

        // 没有返回404
        if (servlet == null) {
            // 404 Not Found:
            PrintWriter pw = response.getWriter();
            pw.write("<h1>404 Not Found </h1><p>No mapping for URL: " + path + "</p>");
            pw.close();
            return;
        }

        // search filter:
        List<Filter> enabledFilters = new ArrayList<>();
        for (FilterMapping mapping : this.filterMappings) {
            if (mapping.matches(path)) {
                enabledFilters.add(mapping.filter);
            }
        }
        Filter[] filters = enabledFilters.toArray(Filter[]::new);
        logger.info("process {} by filter {}, servlet {}", path, Arrays.toString(filters), servlet);
        FilterChain chain = new FilterChainImpl(filters, servlet);
        try {
            chain.doFilter(request, response);
        } catch (ServletException e) {
            logger.info(e.getMessage());
            throw new IOException(e);
        } catch (IOException e) {
            logger.info(e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(String className) throws ServletException {
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found.", e);
        }
        return createInstance(clazz);
    }
    private <T> T createInstance(Class<T> clazz) throws ServletException {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ServletException("Cannot instantiate class " + clazz.getName(), e);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String name, String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("class name is null or empty.");
        }
        Filter filter;
        try {
            filter = createInstance(className);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addFilter(name, filter);
    }
    @Override
    public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null.");
        }
        Filter filter = null;
        try {
            filter = createInstance(clazz);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addFilter(name, filter);
    }
    @Override
    public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter is null.");
        }
        var registration = new FilterRegistrationImpl(this, name, filter);
        this.filterRegistrations.put(name, registration);
        return registration;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return createInstance(clazz);
    }
    @Override
    public FilterRegistration getFilterRegistration(String name) {
        return this.filterRegistrations.get(name);
    }
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.copyOf(this.filterRegistrations);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String name, String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("class name is null or empty.");
        }
        Servlet servlet;
        try {
            servlet = createInstance(className);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addServlet(name, servlet);
    }
    @Override
    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null.");
        }
        Servlet servlet;
        try {
            servlet = createInstance(clazz);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
        return addServlet(name, servlet);
    }
    // 最后都是调用这个方法
    @Override
    public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }
        if (servlet == null) {
            throw new IllegalArgumentException("servlet is null.");
        }

        // 哦, 这里把对应的数据加入到map里面了
        var registration = new ServletRegistrationImpl(this, name, servlet);
        this.servletRegistrations.put(name, registration);
        return registration;
    }

    @Override
    public ServletRegistration getServletRegistration(String s) {
        return this.servletRegistrations.get(s);
    }
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Map.copyOf(this.servletRegistrations);
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return createInstance(clazz);
    }

    @Override
    public String getContextPath() {
        return "";
    }

    // #todo: 这里没看懂在干啥
    @Override
    public ServletContext getContext(String uripath) {
        if ("".equals(uripath)) {
            return this;
        }
        // all others are not exist:
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 6;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 6;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        String defaultMime = "application/octet-stream";
        Map<String, String> mimes = Map.of(
                ".html", "text/html",
                ".txt", "text/plain",
                ".png", "image/png",
                ".jpg", "image/jpeg");
        int n = file.lastIndexOf('.');
        if (n == -1) {
            return defaultMime;
        }
        String ext = file.substring(n);
        return mimes.getOrDefault(ext, defaultMime);
    }

    @Override
    public Set<String> getResourcePaths(String s) {
        return Set.of();
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String s) {
        return null;
    }


    @Override
    public void log(String s) {
    }

    @Override
    public void log(String s, Throwable throwable) {
    }

    @Override
    public String getRealPath(String s) {
        return "";
    }

    @Override
    public String getServerInfo() {
        return "";
    }

    @Override
    public String getInitParameter(String s) {
        return "";
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public boolean setInitParameter(String s, String s1) {
        return false;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {
    }

    @Override
    public void removeAttribute(String s) {
    }

    @Override
    public String getServletContextName() {
        return "";
    }


    @Override
    public ServletRegistration.Dynamic addJspFile(String s, String s1) {
        return null;
    }


    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> set) {
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Set.of();
    }

    @Override
    public void addListener(String s) {
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
    }

    @Override
    public void addListener(Class<? extends EventListener> aClass) {
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void declareRoles(String... strings) {
    }

    @Override
    public String getVirtualServerName() {
        return "";
    }

    @Override
    public int getSessionTimeout() {
        return this.sessionManager.inactiveInterval;
    }

    @Override
    public void setSessionTimeout(int i) {
    }

    @Override
    public String getRequestCharacterEncoding() {
        return "";
    }

    @Override
    public void setRequestCharacterEncoding(String s) {
    }

    @Override
    public String getResponseCharacterEncoding() {
        return "";
    }

    @Override
    public void setResponseCharacterEncoding(String s) {
    }
}
