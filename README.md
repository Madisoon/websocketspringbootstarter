#### 踩坑总结
*因为之前从来没有接触过，所以网上找了很多教程最后发现都不怎么完整，大多数都是直接全盘照抄《Spring Boot》一书中的相关示例，没有做任何修改，没有说明为什么一定要用spring security，而下面的教程自己也是踩了不少坑，如前后分离中session一直获取不到，导致无法链接上，uid不可为中文，websocket自带协议和服务，导致前后端分离一直报跨域的错误等等。当然websocket一些解释在这边就不多说了，相信你已经看了很多教程了，我们就直接上配置教程*
#### 前端配置
*前端配置相对比较简单，主要就是websocket服务的注册，消息的发送和接收。比较关键的点：uid的定义。*

```
    // 用户的特殊标志，一般用id或者生成的uuid，后台为Long，不可带有中文，并且这些值如果从session中获取，需要注意前后端分离带来的session，否则会报错,这些参数主要对应着后台的message类，用于信息的发送
    let from = '';
    let fromName = '';
    let to = 5521;
    let host = window.location.host;
    let webSocket = "";
    // 不同的浏览器对websocket的支持不同
    if ('WebSocket' in window) {
    // 最关键的点，ws必须加上，不可用http，因为两者的协议是不同的，websocket有自带的请求协议，uid是为了将用户的id注册到websocket的服务中
        webSocket = new WebSocket("ws://127.0.0.1:8090/ws?uid=" + from);
    } else if ('MozWebSocket' in window) {
        webSocket = new MozWebSocket("ws://" + host + "/ws" + from);
    } else {
        webSocket = new SockJS("ws://" + host + "/ws/sockjs" + from);
    }
    // 链接，错误，关闭，收到消息相关的回掉函数
    webSocket.onopen = function (event) {
        console.log("WebSocket:已连接");
    };
    webSocket.onerror = function (event) {
        console.log("WebSocket:发生错误 ");
        console.log(event);
    };
    webSocket.onclose = function (event) {
        console.log("WebSocket:已关闭");
        console.log(event);
    };
    webSocket.onmessage = function (event) {
    // 接收到的消息的对象
        let data = JSON.parse(event.data);
    };
    // 发送消息的实例
    function sendMsg() {
        let data = {};
        data["from"] = from;
        data["fromName"] = fromName;
        data["to"] = to;
        data["text"] = "我发给你一条信息";
        webSocket.send(JSON.stringify(data));
    }
```
#### 后端配置

```
先添加maven相关依赖
       <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.2.2</version>
        </dependency>
```

1. ###### 注册websocket服务（前端链接的url地址）

```
import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

 /**
 * 描述:
 * WebScoket配置处理器
 *
 * @author Msater Zg
 * @create 2018-01-24 10:49
 */
@Component
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {
    /**
     * 后台写好服务，项目启动的时候，注册好这两个服务，以供前台调用
     */
    @Resource
    MyWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 链接的时候，websocket会自己增加同源检测的功能，需要单独配置是否允许跨域，我配置*代表允许所有的ip进行调用。
        registry.addHandler(handler, "/ws").addInterceptors(new HandShake()).setAllowedOrigins("*");
        registry.addHandler(handler, "/ws/sockjs").addInterceptors(new HandShake()).setAllowedOrigins("*").withSockJS();
    }
}
```
2.  ###### 握手之前的配置（需要将用户相关uid注册到WebSocketSession中，而这个uid你可以用token，session等等来代替，因为是前端分离，所以我直接用了用户的id）
```
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

 /**
 * 描述:
 * Socket建立连接（握手）和断开
 *
 * @author Msater Zg
 * @create 2018-01-24 10:49
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
```
3. ###### 消息推送的相关的后台的方法

```
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

  /**
 * 描述:
 * Socket处理器(包括发送信息，接收信息，信息错误等方法。)
 *
 * @author Msater Zg
 * @create 2018-01-24 10:49
 */
@Component
public class MyWebSocketHandler implements WebSocketHandler {
    /**
     * 最重要的websocket处理程序（包括发送信息，接收信息，信息错误等方法。）
     */

    /**
     * 先注册一个websocket服务器，将连接上的所有用户放进去
     */
    public static final Map<Long, WebSocketSession> USER_SOCKET_SESSION_MAP;

    static {
        USER_SOCKET_SESSION_MAP = new HashMap<Long, WebSocketSession>();
    }

    /**
     * 前台连接并且注册了账户
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long uid = (Long) session.getAttributes().get("uid");
        if (USER_SOCKET_SESSION_MAP.get(uid) == null) {
            USER_SOCKET_SESSION_MAP.put(uid, session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message.getPayloadLength() == 0) {
            return;
        }
        NotificationMessage msg = new Gson().fromJson(message.getPayload().toString(), NotificationMessage.class);
        msg.setDate(new Date());
        sendMessageToUser(msg.getTo(), new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));
    }

    /**
     * 消息传输错误处理，如果出现错误直接断开连接
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        removeWebSocketUser(session);
    }

    /**
     * 关闭连接后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("Websocket:" + session.getId() + "已经关闭");
        removeWebSocketUser(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param message
     * @throws IOException
     */
    public void broadcast(final TextMessage message) throws IOException {
        Iterator<Entry<Long, WebSocketSession>> it = USER_SOCKET_SESSION_MAP.entrySet().iterator();
        // 多线程群发（给所有在线的用户发送消息）  先判断是否里面有用户（）然后循环发消息
        /*后台调用sendMessage方法的时候，前台会触发onmessage*/
        while (it.hasNext()) {
            final Entry<Long, WebSocketSession> entry = it.next();
            if (entry.getValue().isOpen()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (entry.getValue().isOpen()) {
                                entry.getValue().sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * 单个用户发消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessageToUser(Long uid, TextMessage message) throws IOException {
        //根据传过来的账号，在websocketseesion的服务器里面找，接收者注册的账号
        WebSocketSession session = USER_SOCKET_SESSION_MAP.get(uid);
        if (session != null && session.isOpen()) {
            session.sendMessage(message);
        }
    }

    public void removeWebSocketUser(WebSocketSession session) {
        Iterator<Entry<Long, WebSocketSession>> it = USER_SOCKET_SESSION_MAP.entrySet().iterator();
        // 移除Socket会话
        while (it.hasNext()) {
            Entry<Long, WebSocketSession> entry = it.next();
            if (entry.getValue().getId().equals(session.getId())) {
                USER_SOCKET_SESSION_MAP.remove(entry.getKey());
                break;
            }
        }
    }
}
```
4. ###### 消息类（可根据不同的需求进行修改）

```
import java.util.Date;
/**
 * 描述:
 * 消息类
 *
 * @author Msater Zg
 * @create 2018-02-24 10:49
 */
public class NotificationMessage {
    /**
     * 发送者账号
     */
    public Long from;
    /**
     * 发送者名称
     */
    public String fromName;
    /**
     * 接收者账号
     */
    public Long to;
    /**
     * 发送的内容
     */
    public String text;
    /**
     * 发送的日期
     */
    public Date date;

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
```
#### 后续
大致原理就是后台启动websocket服务，前端用户注册并在websocket自带session服务中放这儿值，而后续的消息发送，就是给websocketsession中的用户发送消息，然后前端进行响应，以上都是自己一些看法，有什么问题还希望指出
##### 最后附上自己封装的starter源码git地址（欢迎star）：https://github.com/zg091418/websocketspringbootstarter




