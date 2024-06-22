package com.bool.jerrymouse.engine;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : 不二
 * @date : 2024/6/3-11:06
 * @desc : 服务器端的session管理器
 **/
public class SessionManager implements Runnable {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // 引用ServletContext:
    ServletContextImpl servletContext;
    // 持有SessionID -> Session:
    Map<String, HttpSessionImpl> sessions = new ConcurrentHashMap<>();
    // Session默认过期时间(秒):
    int inactiveInterval;

    public SessionManager(ServletContextImpl servletContext, int interval) {
        this.servletContext = servletContext;
        this.inactiveInterval = interval;
        Thread t = new Thread(this, "Session-Cleanup-Thread");
        t.setDaemon(true);
        t.start();
    }


    // 根据SessionID获取一个Session:
    public HttpSession getSession(String sessionId) {
        HttpSessionImpl session = sessions.get(sessionId);
        if (session == null) {
            // Session未找到，创建一个新的Session:
            session = new HttpSessionImpl(this.servletContext, sessionId, inactiveInterval);
            sessions.put(sessionId, session);
        } else {
            // Session已存在，更新最后访问时间:
            session.lastAccessedTime = System.currentTimeMillis();
        }
        return session;
    }

    // 删除Session:
    public void remove(HttpSession session) {
        this.sessions.remove(session.getId());
    }

    // 扫描线程, 过期session
    @Override
    public void run() {
        for(;;){
            // 每60s扫描一次
            try {
                Thread.sleep(60_000L);
            }catch (InterruptedException e){
                break;
            }
            long now = System.currentTimeMillis();
            for(String sessionId : sessions.keySet()) {
                HttpSession session = sessions.get(sessionId);
                if (session.getLastAccessedTime() + session.getMaxInactiveInterval()*1000L < now) {
                    logger.warn("remove expired session: {}, last access time: {}", sessionId, session.getLastAccessedTime());
                    session.invalidate();
                }
            }
        }
    }
}
