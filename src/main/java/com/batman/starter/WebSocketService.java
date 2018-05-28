package com.batman.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;

/**
 * Description:
 * websocket服务类
 *
 * @author Msater Zg
 * @create 2018/5/28 下午4:03
 */

@Service
public class WebSocketService {

    @Autowired
    MyWebSocketHandler myWebSocketHandler;

    /**
     * 给所有人发信息
     *
     * @param message
     */
    public void sendMessage(TextMessage message) {
        try {
            myWebSocketHandler.broadcast(message);
        } catch (IOException e) {

        }
    }

    /**
     * 给个人发信息
     *
     * @param message
     */
    public void sendMessage(Long uid, TextMessage message) {
        try {
            myWebSocketHandler.sendMessageToUser(uid, message);
        } catch (IOException e) {

        }
    }

    /**
     * 给部分人发信息
     *
     * @param uidS
     * @param message
     */
    public void sendMessage(List<Long> uidS, TextMessage message) {
        for (Long uid : uidS) {
            try {
                myWebSocketHandler.sendMessageToUser(uid, message);
            } catch (IOException e) {

            }
        }
    }
}
