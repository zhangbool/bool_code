package com.bool.jerrymouse.engine;

import com.bool.jerrymouse.engine.support.Attributes;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

/**
 * @author : 不二
 * @date : 2024/6/3-11:06
 * @desc : session实现
 **/
public class HttpSessionImpl implements HttpSession {

    ServletContextImpl servletContext; // ServletContext
    String sessionId; // SessionID
    int maxInactiveInterval; // 过期时间(s)
    long creationTime; // 创建时间(ms)
    long lastAccessedTime; // 最后一次访问时间(ms)
    Attributes attributes; // getAttribute/setAttribute

    HttpSessionImpl(ServletContextImpl servletContext, String sessionId, int maxInactiveInterval) {
        this.servletContext = servletContext;
        this.sessionId = sessionId;
        this.creationTime = this.lastAccessedTime = System.currentTimeMillis();
        this.attributes = new Attributes(true);
        setMaxInactiveInterval(maxInactiveInterval);
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return this.attributes.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return this.attributes.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
        } else {
            this.attributes.setAttribute(name, value);
        }
    }

    void checkValid() {
        if (this.sessionId == null) {
            throw new IllegalStateException("Session is already invalidated.");
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        this.attributes.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        this.servletContext.sessionManager.remove(this);
        // 这个是干啥, 对象都删掉了, 这里不设null也没事吧
        this.sessionId = null;
    }

    @Override
    public boolean isNew() {
        return this.creationTime == this.lastAccessedTime;
    }
}
