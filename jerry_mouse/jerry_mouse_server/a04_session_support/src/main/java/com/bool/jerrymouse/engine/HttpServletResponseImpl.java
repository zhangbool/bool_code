package com.bool.jerrymouse.engine;

import com.bool.jerrymouse.connector.HttpExchangeResponse;
import com.bool.jerrymouse.engine.support.HttpHeaders;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author : 不二
 * @date : 2024/5/29-15:11
 * @desc :
 **/
public class HttpServletResponseImpl implements HttpServletResponse {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    final HttpExchangeResponse exchangeResponse;
    int status = 200;
    final HttpHeaders headers;

    boolean committed = false;

    public HttpServletResponseImpl(HttpExchangeResponse exchangeResponse) {
        this.exchangeResponse = exchangeResponse;
        this.headers = new HttpHeaders(exchangeResponse.getResponseHeaders());
        this.setContentType("text/html");

        // 这个没啥用吧, 就一个适配器
        // this.setContentType("text/html");
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        this.exchangeResponse.sendResponseHeaders(this.status, 0);
        return new PrintWriter(this.exchangeResponse.getResponseBody(), true, StandardCharsets.UTF_8);
    }

    @Override
    public void setContentType(String type) {
        setHeader("Content-Type", type);
    }

    @Override
    public void setHeader(String name, String value) {
        this.exchangeResponse.getResponseHeaders().set(name, value);
    }


    // not implemented yet:

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return "";
    }

    @Override
    public String encodeRedirectURL(String s) {
        return "";
    }


    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        PrintWriter pw = getWriter();
        // 默认状态码是200, 这里如果传入新的状态码会进行覆盖
        pw.write(String.format("<h1>%d %s</h1>", sc, msg));
        pw.close();
    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

        logger.info("重定向到----{}", location);

        checkNotCommitted();
        this.status = 302;
        this.headers.setHeader("Location", location);
        commitHeaders(-1);
    }

    void commitHeaders(long length) throws IOException {
        this.exchangeResponse.sendResponseHeaders(this.status, length);
        this.committed = true;
    }

    // check if not committed:
    void checkNotCommitted() {
        if (this.committed) {
            throw new IllegalStateException("Response is committed.");
        }
    }

    @Override
    public void setDateHeader(String s, long l) {

    }

    @Override
    public void addDateHeader(String s, long l) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setIntHeader(String s, int i) {

    }

    @Override
    public void addIntHeader(String s, int i) {

    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return "";
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return List.of();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return List.of();
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }



    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }



    @Override
    public void setCharacterEncoding(String s) {

    }

    @Override
    public void setContentLength(int i) {

    }

    @Override
    public void setContentLengthLong(long l) {

    }


    @Override
    public void setBufferSize(int i) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
