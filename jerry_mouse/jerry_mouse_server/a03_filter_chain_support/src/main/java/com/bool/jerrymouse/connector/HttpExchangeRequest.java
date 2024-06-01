package com.bool.jerrymouse.connector;

import java.net.URI;

// 这里直接写成内部接口
public interface HttpExchangeRequest {
    String getRequestMethod();
    URI getRequestURI();
}
