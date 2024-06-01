package com.bool.jerrymouse.engine;

import com.bool.jerrymouse.engine.mapping.ServletMapping;
import com.bool.jerrymouse.utils.AnnoUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    // 上下文中hold一个对象集合, 后续用来保持状态
    final List<ServletMapping> servletMappings = new ArrayList<>();
    private final Map<String, ServletRegistrationImpl> servletRegistrations = new HashMap<>();

    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getRequestURI();

        // search servlet:
        // 如果能找到, 就使用之前的servlet进行服务
        // 这个用的是: ServletMapping集合.
        // 这里不太明白的地方:
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

        servlet.service(request, response);
    }

    // 这里是servlet的map
    // 然后上面还有个servlet的集合
    // 然后上面还有个servlet的包装类的map
    // 这个怎么要包装这么多...
    // 所以这个map是干啥的呢, 好像没啥用吧
    final Map<String, Servlet> nameToServlets = new HashMap<>();
    public void initialize(List<Class<?>> servletClasses) {
        // 根据类来获取带有@WebServlet的类
        // 然后根据获取的类拿到相关的内容进行创建servlet
        for (Class<?> c : servletClasses) {
            WebServlet ws = c.getAnnotation(WebServlet.class);
            if (ws != null) {
                System.out.println("auto register @WebServlet: " +  c.getName());
                @SuppressWarnings("unchecked")
                Class<? extends Servlet> clazz = (Class<? extends Servlet>) c;
                ServletRegistration.Dynamic registration = this.addServlet(AnnoUtils.getServletName(clazz), clazz);

                // 先加到mapping里, 后面需要取出来
                registration.addMapping(AnnoUtils.getServletUrlPatterns(clazz));

                // 暂时不知道这一步是干啥的
                 registration.setInitParameters(AnnoUtils.getServletInitParams(clazz));
            }
        }

        // init servlets:
        for (String name : this.servletRegistrations.keySet()) {
            // 遍历所以的数据, 然后做处理
            var registration = this.servletRegistrations.get(name);
            try {
                // 把对应的配置信息设置到sevlet里面去
                registration.servlet.init(registration.getServletConfig());

                this.nameToServlets.put(name, registration.servlet);
                // 这里为啥是一个集合, 一个servlet能有多个映射吗
                // 是的, 传递的时候可以传递一个数组
                // 但是这样的话, 也就只有servletMappings是拆分后的. 其他的也不是吧, 先往后看
                for (String urlPattern : registration.getMappings()) {
                    this.servletMappings.add(new ServletMapping(urlPattern, registration.servlet));
                }
                registration.initialized = true;

            } catch (ServletException e) {
                System.out.println("init servlet failed: " + name + " / " + registration.servlet.getClass().getName() + ": " + e.getMessage());
            }
        }

        // 排序的目的是为了啥
        // 这是干啥的呢
        // important: sort mappings:
        Collections.sort(this.servletMappings);
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
    public ServletRegistration.Dynamic addServlet(String name, String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("class name is null or empty.");
        }
        Servlet servlet = null;
        try {
            // #todo: 这里没太明白: 这里不是重复了吗
            // Class<? extends Servlet> clazz = createInstance(className);
            // servlet = createInstance(clazz);

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
        return null;
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
    public FilterRegistration.Dynamic addFilter(String s, String s1) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> aClass) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String s) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Map.of();
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
        return 0;
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
