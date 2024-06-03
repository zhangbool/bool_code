package com.bool.jerrymouse.connector;

import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

// 这里直接写成内部接口
public interface HttpExchangeRequest {
    String getRequestMethod();
    URI getRequestURI();


    Headers getRequestHeaders();
    InetSocketAddress getRemoteAddress();
    InetSocketAddress getLocalAddress();
    byte[] getRequestBody() throws IOException;
}
