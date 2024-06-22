package com.bool.jerrymouse.connector;

import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.io.OutputStream;

// 这里直接写成内部接口
public interface HttpExchangeResponse {
    Headers getResponseHeaders();
    void sendResponseHeaders(int rCode, long responseLength) throws IOException;
    OutputStream getResponseBody();
}
