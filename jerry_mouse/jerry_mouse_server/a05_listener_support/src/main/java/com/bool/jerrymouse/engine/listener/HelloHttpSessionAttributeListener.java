package com.bool.jerrymouse.engine.listener;

import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : 不二
 * @date : 2024/6/5-14:50
 * @desc :
 **/
public class HelloHttpSessionAttributeListener implements HttpSessionAttributeListener {

    Logger logger = LoggerFactory.getLogger(HelloHttpSessionAttributeListener.class);

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        logger.info(">>> HttpSession attribute added: {} = {}", event.getName(), event.getValue());
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        logger.info(">>> HttpSession attribute removed: {} = {}", event.getName(), event.getValue());
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        logger.info(">>> HttpSession attribute replaced: {} = {}", event.getName(), event.getValue());
    }
}
