package com.iboxpay.settlement.gateway.common.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.msg.MessageCenter;

@Controller
@RequestMapping("/manage/message")
public class MessageController {

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView mv = new ModelAndView();
        mv.addObject("messages", MessageCenter.getMessages());
        mv.setViewName("/views/message/list");
        return mv;
    }

    @RequestMapping(value = "has-lastest-message.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> hasLastestMessage() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("hasLastestMessage", String.valueOf(MessageCenter.hasLastestMessage()));
        return result;
    }

    @RequestMapping(value = "unread-count.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getUnreadCount() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("unreadSize", String.valueOf(MessageCenter.getUnreadSize()));
        result.put("hasLastestMessage", String.valueOf(MessageCenter.hasLastestMessage()));
        return result;
    }

    @RequestMapping(value = "receive-lastest.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> receiveLastest() {
        MessageCenter.receiveLastest();
        return new HashMap<String, String>();
    }

    @RequestMapping(value = "set-read.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> setRead(@RequestParam(value = "key", required = false) String key) {
        MessageCenter.setRead(key);
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("read", "true");
        return result;
    }
}
