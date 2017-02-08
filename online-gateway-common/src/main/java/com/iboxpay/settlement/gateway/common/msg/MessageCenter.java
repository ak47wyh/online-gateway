package com.iboxpay.settlement.gateway.common.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageCenter {

    private static Logger logger = LoggerFactory.getLogger(MessageCenter.class);

    private static long receiveMessageTime;
    private static long lastestMessageTime;//最新一条消息的时间
    private static int MESSAGE_SIZE = 200;
    //只保留200条
    private static List<Message> messages = new ArrayList<Message>(MESSAGE_SIZE);

    /**
     * 投递消息
     * @param message
     */
    public synchronized static void deliver(Message message) {
        try {
            doDeliver(message);
        } catch (Exception e) {
            logger.error("投递消息失败", e);
        }
    }

    /**
     * 投递消息
     * @param message
     */
    private static void doDeliver(Message message) {
        if (message.getKey() == null) throw new NullPointerException("message key is null");

        Date now = new Date();
        Message existMessage = null;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getKey().equals(message.getKey())) {
                existMessage = messages.remove(i);
                message.setFirstTime(existMessage.getFirstTime());
                message.setCount(existMessage.getCount() + 1);
                message.setLastTime(now);
                messages.add(0, message);
                break;
            }
        }
        if (existMessage == null) {
            message.setFirstTime(now);
            message.setLastTime(now);
            message.setCount(1);
            messages.add(0, message);
        }
        lastestMessageTime = System.currentTimeMillis();
        while (messages.size() > MESSAGE_SIZE) {
            messages.remove(0);
        }
    }

    /**
     * 已读取最新消息
     */
    public synchronized static void receiveLastest() {
        receiveMessageTime = lastestMessageTime;
    }

    public synchronized static List<Message> getMessages() {
        return messages;
    }

    /**
     * 是否有新的消息
     * @return
     */
    public synchronized static boolean hasLastestMessage() {
        return lastestMessageTime > receiveMessageTime;
    }

    /**
     * 设置为已读
     * @param key
     */
    public synchronized static void setRead(String key) {
        for (Message message : messages) {
            if (message.getKey().equals(key)) {
                message.setRead(true);
            }
        }
    }

    /**
     * 获取未读取的消息数
     * @return
     */
    public synchronized static int getUnreadSize() {
        int size = 0;
        for (Message message : messages) {
            if (!message.isRead()) size++;
        }
        return size;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add(0, "a");
        list.add(0, "b");
        list.remove(1);
        list.add(0, "a");
        System.out.println(Arrays.toString(list.toArray(new String[0])));
    }
}
