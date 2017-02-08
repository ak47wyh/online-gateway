<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号编辑 - 银企直联中间件</title>
<style>
.txt_input {
	width: 100%;
}
.txt_input_array {
	width: 80%;
}
</style>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
</head>
<body>
<a href="list.htm">返回交易账号列表</a><br/>
${errorMsg }

【${account.bankFullName}】公司账号配置：
<form action="edit.htm" method="post">
<input type="hidden" name="id" value="${paymentMerchant.id}"/>
<table style="width: 1000px">
<thead>
<tr><th>账户属性</th><th width="50%">配置值</th></tr>
</thead>
<tr><td style="border-bottom:1pt solid black;" colspan="2"></td></tr>
<tr><td>通道账户名称：</td><td><input name="appCode" value="${paymentMerchant.appCode }"  class="txt_input" readonly="readonly"/></td></tr>
<tr><td>AppId：</td><td><input name="appId" value="${paymentMerchant.appId }"  class="txt_input" readonly="readonly"/></td></tr>
<tr><td>appSecret：</td><td><input name="appSecret" value="${paymentMerchant.appSecret }"  class="txt_input" readonly="readonly"/></td></tr>
<tr><td>subAppId：</td><td><input name="subAppId" value="${paymentMerchant.subAppId }"  class="txt_input"/></td></tr>
<tr><td>subAppSecret：</td><td><input name="subAppSecret" value="${paymentMerchant.subAppSecret }"  class="txt_input"/></td></tr>
<tr><td>交易商户名称：</td><td><input name="payMerchantName" value="${paymentMerchant.payMerchantName }" class="txt_input"/></td></tr>
<tr><td>交易商户号：</td><td><input name="payMerchantNo" value="${paymentMerchant.payMerchantNo }" class="txt_input" readonly="readonly"/></td></tr>
<tr><td>交易子商户号：</td><td><input name="payMerchantSubNo" value="${paymentMerchant.payMerchantSubNo }" class="txt_input" readonly="readonly"/></td></tr>
<tr><td>交易密钥：</td><td><input name="payMerchantKey" value="${paymentMerchant.payMerchantKey }" class="txt_input"/></td></tr>
<tr><td></td><td><input type="submit" value="提交"/>
<c:if test="${!empty input_error}">
	<font color='red'>${input_error}</font>
</c:if>
${success==true?"<font color='green'>提交成功!</font>":"" }
</td><td></td></tr>
</table>
</form>

</body>
</html>
