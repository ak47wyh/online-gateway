package com.iboxpay.settlement.gateway.kq;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.config.Property.Type;

public class KQFrontEndConfig extends FrontEndConfig {

    private static final long serialVersionUID = 1L;
    /*接口版本[支付提交版本]*/
    private Property version;
    /*接口版本[查询]*/
    private Property queryVersion;
    /*页码*/
    private Property page;
    /*页码显示条数*/
    private Property pageSize;

    /*单笔免签约支付地址*/	
	private Property debitSingleType;
	/*电笔免签约查询地址*/
	private Property debitSingleQueryType;
	/*批量免签约支付地址*/
	private Property debitbatchType;
	/*批量免签约查询明细地址*/
	private Property debitbatchQueryType;
    
    /*加密方式*/
    private Property featureCode;
	/* 币种 */
    private Property cur ;

    private Property uri;


    
    public KQFrontEndConfig() {
        //这些看父类
        setDefVal(protocal, "https");
        setDefVal(charset, "GBK");

        version = new Property("version", "支付接口版本");
        queryVersion = new Property("queryVersion", "查询接口版本");
        page = new Property("page", "页码");
        pageSize= new Property("pageSize", "页码显示条数");

        debitSingleType= new Property("debitSingleType", "单笔免签约支付地址");
        debitSingleQueryType= new Property("debitSingleQueryType", "电笔免签约查询地址");
        debitbatchType=new Property("debitbatchType","批量免签约支付地址");
        debitbatchQueryType=new Property("debitbatchQueryType","批量免签约查询明细地址");
        
        featureCode= new Property("featureCode", "加密方式");
        cur= new Property("cur", "【币种】默认RMB");
		
        
        
		uri = new Property("uri", "/ddpproduct/services", "http请求的URI(若与默认不同请设置)");
    }



	public Property getVersion() {
		return version;
	}

	public Property getPageSize() {
		return pageSize;
	}

	

	public Property getQueryVersion() {
		return queryVersion;
	}



	public Property getPage() {
		return page;
	}

	public Property getDebitSingleType() {
		return debitSingleType;
	}

	public Property getDebitSingleQueryType() {
		return debitSingleQueryType;
	}

	public Property getDebitbatchType() {
		return debitbatchType;
	}

	public Property getDebitbatchQueryType() {
		return debitbatchQueryType;
	}

	public Property getFeatureCode() {
		return featureCode;
	}

	public Property getCur() {
		return cur;
	}

	public Property getUri() {
		return uri;
	}
	
	
}

