package com.iboxpay.settlement.gateway.common.web;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.dao.AccountDao;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.FrontEndDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.util.Sequence;

//测试专用，懒得弄单元测试了
@Path("/test")
@Service
public class TestController {

    ////"serviceCode":"transfer" 兼容，目前只能手动判断 
    //	private final static Pattern p = Pattern.compile("\"serviceCode\"\\s*:\\s*\"([^\\\"]+)\"");

    @Path("")
    @GET
    public String index() {
        return "hello world!";
    }

    @Path("/sequence")
    @GET
    public String genSequence(@QueryParam("num") int num) {
        long t = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        while (num-- > 0) {
            sb.append(Sequence.genSequence());
            sb.append("<br/>");
        }
        t = System.currentTimeMillis() - t;
        return "total used " + t + " ms. <br/><br/>" + sb.toString();
    }

    @Resource
    AccountDao accountDao;

    CommonDao commonDao = CommonDaoImpl.getDao(AccountEntity.class);

    FrontEndDao feDao;

    @Resource
    public void setFeDao(FrontEndDao feDao) {
        this.feDao = feDao;
    }

    //	@Path("/feconfig")
    //	@GET
    //	public String feconfig(){
    //		CcbFrontEndConfig ccbFeConfig = (CcbFrontEndConfig)feDao.get(1);
    //		if(ccbFeConfig == null){
    //			ccbFeConfig = new CcbFrontEndConfig();
    //			ccbFeConfig.setId(1);
    //			ccbFeConfig.setName("ccb-1");
    //		}
    //		ccbFeConfig.getCustId().setVal("custID----1");
    //		feDao.update(ccbFeConfig);
    //		return "sucess";
    //	}

    //	private static final Property testProperty = new Property("testProperty", Type.array, "测试属性").asConfig();
    //	private static final Property testProperty2 = new Property("testProperty2", "测试属性2").asConfig();

    @Path("/property")
    @GET
    public String testProperty(@QueryParam("value") String value[]) {
        if (value != null) {
            //			testProperty.setVals(value);
            //			ConfPropertyManager.save(testProperty);
        }
        //		return "value: "+ Arrays.toString(testProperty.getVals());
        return null;
    }

    @Resource
    PaymentDao paymentDao;

    @Path("/testPay")
    @GET
    public String testPay(@QueryParam("batchSeqId") String batchSeqId) {
        BatchPaymentEntity batchPaymentEntity = paymentDao.getBatchPaymentEntity(batchSeqId);
        batchPaymentEntity.getPaymentEntitys();
        if (batchPaymentEntity != null) {
            return "not null";
        } else {
            return "ok";
        }
    }
}
