package com.batman.starter;

import java.util.Date;

/**
 * Description:
 * 消息类
 *
 * @author Msater Zg
 * @create 2018/5/28 下午4:00
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
