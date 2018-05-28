package com.batman.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 * websocket相关配置
 *
 * @author Msater Zg
 * @create 2018/5/28 下午4:20
 */
@ConfigurationProperties(prefix = "webscoket")
public class WebSocketProperties {

    private String handlerUrl;

    private String handlerUrlSock;

    private String handlerOrigin;

    public String getHandlerUrl() {
        return handlerUrl;
    }

    public void setHandlerUrl(String handlerUrl) {
        this.handlerUrl = handlerUrl;
    }

    public String getHandlerUrlSock() {
        return handlerUrlSock;
    }

    public void setHandlerUrlSock(String handlerUrlSock) {
        this.handlerUrlSock = handlerUrlSock;
    }

    public void setHandlerOrigin(String handlerOrigin) {
        this.handlerOrigin = handlerOrigin;
    }

    public String getHandlerOrigin() {
        return handlerOrigin;
    }
}
