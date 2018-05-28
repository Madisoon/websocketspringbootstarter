package com.batman.starter;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebScoket配置处理器
 *
 * @author zg
 * @Date 2015年6月11日 下午1:15:09
 */
@Component
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {
    /**
     * 后台写好服务，项目启动的时候，注册好这两个服务，以供前台调用
     */
    @Resource
    MyWebSocketHandler handler;

    @Autowired
    WebSocketProperties webSocketProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 链接的时候，websocket会自己增加同源检测的功能，需要单独配置是否允许跨域。
        registry.addHandler(handler, webSocketProperties.getHandlerUrl()).addInterceptors(new HandShake()).setAllowedOrigins(webSocketProperties.getHandlerOrigin());
        registry.addHandler(handler, webSocketProperties.getHandlerUrlSock()).addInterceptors(new HandShake()).setAllowedOrigins(webSocketProperties.getHandlerOrigin()).withSockJS();
    }
}