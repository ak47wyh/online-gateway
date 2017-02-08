package com.iboxpay.settlement.gateway.common.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.service.utils.XmlUtils;

@Controller
@RequestMapping("/jd")
public class JdController {

    private static Logger logger = LoggerFactory.getLogger(JdController.class);

    @Resource
    private BankTransController bankTransController;

    @RequestMapping(value = "{trans}/notify.htm", method = RequestMethod.POST)
    @ResponseBody
    public void notify(HttpServletRequest request, HttpServletResponse response, @PathVariable("trans") String callbackCode) {
        try {
            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String resString = XmlUtils.parseRequst(request);
            if ("pay".equals(callbackCode)) {
                logger.info("支付异步通知内容：" + resString);
            } else if ("refund".equals(callbackCode)) {
                logger.info("退款异步通知内容：" + resString);
            }

            String respString = "fail";
            if (resString != null && !"".equals(resString)) {
                Map<String, Object> resultMap = JsonUtil.parseJSON2Map(resString);
                resultMap.put("callbackCode", callbackCode);
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("resultMap", resultMap);
                param.put("appCode", "jdpay");
                param.put("type", "online");
                param.put("requestSystem", "online_sys");
                String reqContext = JsonUtil.toJson(param);
                String transCode = "callback";
                String result = bankTransController.trans(transCode, reqContext, request);
                Map appResultMap = JsonUtil.parseJSON2Map(result);
                String rsStatus = (String) appResultMap.get("status");
                if (rsStatus.equals("success")) {
                    List list = (List) appResultMap.get("data");
                    Map dataMap = (Map) list.get(0);
                    String status = (String) dataMap.get("status");
                    if (status.equals("success") || "refundsuccess".equals(status)) {
                        respString = "SUCCESS";
                    } else {
                        respString = "FAIL";
                    }
                }
            }
            response.getWriter().write(respString);
        } catch (Exception e) {
            logger.error("京东异步通知消息异常：" + e);
        }
    }
}
