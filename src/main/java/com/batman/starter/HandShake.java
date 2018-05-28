package com.batman.starter;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Socket建立连接（握手）和断开
 *
 * @author caizi
 * @Date 2015年6月11日 下午2:23:09
 */
public class HandShake implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        /**
         * websocket系统启动连接程序，启动的时候就会把他的session值传过来，放入到websocketsession（websocket的一个内置服务器）里面
         */
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        Long uid = Long.parseLong(servletRequest.getServletRequest().getParameter("uid"));
        if (uid != 0) {
            attributes.put("uid", uid);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
    }
}