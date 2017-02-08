package com.iboxpay.gateway.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.web.BankTransController;

public class PaymentBatchTest {

    public static void main(String[] args) {
        /*String detail=
        	"{"+
        	"	\"seqId\" : \"{detailId}\",\n"+
        	"	\"amount\" : \"0.01\",\n"+
        	"	\"accNo\" : \"6222802920811230{detailId}\",\n"+
        	"	\"accName\" : \"罗建\",\n"+
        	"	\"accType\" : \"2\",\n"+
        	"	\"bankName\" : \"ccb\",\n"+
        	"	\"bankFullName\" : \"中国建设银行\"\n" +
        	"   }";
        StringBuffer sb = new StringBuffer();
        for(int i=1; i<= 500; i++){
        	if(i > 1)
        		sb.append(",");
        	
        	sb.append(detail.replaceAll("\\{detailId\\}", i+""));
        }
        System.out.println(sb);*/
        /*String src = "王秀嫚";
        String charsetName = "GB2312";
        convert(src, charsetName);
        
        test1();*/

        getFormat();
        
        
       int num = 46;
       for (int i = 0; i < 2; i++) {
    	   num++;
    	   String pack =  "{"
            + "\"appCode\": \"xmcmbcdk\","
            + "\"batchSeqId\": \"2016030700" + num + "\","
            + "\"data\": ["
            + "{"
            + "\"accType\": \"2\","
            + "\"cardType\": \"0\","
            + "\"amount\": \"0.01\","
            + "\"accNo\": \"6226220613059780\","
            + "\"bankBranchName\": \"民生银行科苑支行\","
            + "\"cnaps\": \"305393000028\","
            + "\"accName\": \"韦元话\","
            + "\"cnapsBankNo\": \"305393000028\","
            + "\"seqId\": \"2016030700" + num + "\","
            + "\"ankFullName\": \"民生银行\","
            + "\"extProperties\": {\"certType\":\"0\",\"certNo\":\"452127198105100954\",\"mobileNo\":\"13928783530\"}"
            + "}"
            + "],"
            + "\"type\": \"pay\","
            + "\"requestSystem\": \"online_sys\""
            + "}";
       
    	   BankTransController bankTransController = (BankTransController) SystemManager.getSpringContext().getBean("bankTransController");
    	   bankTransController.trans(TransCode.PAY.getCode(), pack, null);
       }
    }

    public static void convert(String src, String charsetName) {
        try {
            String des = new String(src.getBytes(charsetName), charsetName);
            System.out.println(des);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static void test1() {
        String s1 = "123\r456";
        String s2 = "123\n456";
        String s3 = "123\r\n456";
        String s4 = "123\n\r456";
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
    }

    private static void getFormat() {
        Map<String, Object> requestModel = new HashMap<String, Object>();
        //		List<SettleAccountModel> list = settlePatchModel.getLisa();
        //批次号
        requestModel.put("batchSeqId", "123456"/*getBatchSeqId(settleAccountModel)*/);
        //出款账号
        requestModel.put("bankName", "");
        requestModel.put("accNo", "");
        requestModel.put("requestSystem", "settle_sys");
        requestModel.put("type", "2");
        //以下添加收款信息
        List<Map<String, String>> customerInfos = new ArrayList<Map<String, String>>();
        requestModel.put("customerInfos", customerInfos);
        //		for(SettleAccountModel settleAccountModel : list){
        Map<String, String> customerInfo = new HashMap<String, String>();
        customerInfos.add(customerInfo);
        customerInfo.put("seqId", "111");
        customerInfo.put("amount", "100");
        customerInfo.put("accNo", "123456");
        customerInfo.put("accName", "");
        customerInfo.put("accType", "");
        //		customerInfo.put("bankName" , bank);
        customerInfo.put("cnaps", "");
        customerInfo.put("bankBranchName", "");
        customerInfo.put("bankFullName", "");
        customerInfo.put("cnapsBankno", "");//网银支付号
        Map<String, String> extProperties = new HashMap<String, String>();
        //			extProperties.put("certNo", "");
        extProperties.put("clearMerchNo", "20150506");
        customerInfo.put("extProperties", JsonUtil.toJson(extProperties));
        //		}

        System.out.println(JsonUtil.toJson(requestModel));

    }
}
