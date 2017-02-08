package com.iboxpay.settlement.gateway.common.msg;

import java.util.Date;

import com.iboxpay.settlement.gateway.common.util.ClassUtil;

public class Message {

    public enum Color {
        BLACK, GREEN, RED
    }

    private String key;
    private String title;
    private String message;
    private boolean read;//是否已读
    private int count;//产生消息的次数
    private Color color;
    private Date firstTime;
    private Date lastTime;

    public Message(String key, String title, String message, Color color) {
        this.key = key;
        this.title = title;
        this.message = message;
        this.color = color;
    }

    public Message(String key, String title, String message) {
        this(key, title, message, Color.BLACK);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Date getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Date firstTime) {
        this.firstTime = firstTime;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
