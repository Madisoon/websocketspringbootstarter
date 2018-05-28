package com.batman.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * websokcet启动类
 *
 * @author Msater Zg
 * @create 2018/5/28 下午4:00
 */
@Configuration
@ConditionalOnClass(WebSocketService.class)
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketAutoConfiguration {
    @Bean
    WebSocketService webSocketService() {
        return new WebSocketService();
    }

    @Bean
    MyWebSocketHandler myWebSocketHandler() {
        return new MyWebSocketHandler();
    }


}
