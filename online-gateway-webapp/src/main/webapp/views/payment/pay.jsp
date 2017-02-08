<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号编辑 - 银企直联中间件</title>
<link rel="stylesheet" rev="stylesheet" href="../../css/style.css" type="text/css" media="all" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<style>
.txt_input {
	width: 100%;
}
</style>

</head>

<body>
<script type="text/javascript">

function payMent(){
	

	var payAcc      = $("#payAcc").attr("value");
	var payName     = $("#payName").attr("value");
	var recAcc      = $("#recAcc").attr("value");
	var recName     = $("#recName").attr("value");
	var recBank     = $("#recBank").attr("value");
	var recBankNo   = $("#recBankNo").attr("value");
	var tranAmount  = $("#tranAmount").attr("value");
	var currency    = $("#currency").attr("value");
	var remark      = $("#remark").attr("value");
	
	var batchSeqId  = "";
	
	alert(payAcc);
	
	for(var i=0;i<6;i++) 
	{ 
		batchSeqId+=Math.floor(Math.random()*10); 
	}
    $.ajax({
     	type: "POST",
     	url:"${pageContext.request.contextPath}/services/online/pay", 
     	dataType:"json",
     	data:JSON.stringify({
			"batchSeqId" : batchSeqId,
			"appCode" :	payAcc,
			"customerInfos": [
			                  
// 								{	  
// 									  "seqId":"0",
// 									  "amount":"0.01",
// 									  "accNo":"7315010182600029713",
// 									  "accName":"中信11038954",
// 									  "bankFullName":"中信银行",
// 									  "cnaps":"302290031501",
// 									  "remark":"测试",
// 									  "accType":1
// 								}                  
			                  
// 			                  {	  
// 			                	  "seqId":"0",
// 			                	  "amount":tranAmount,
// 			                	  "accNo":recAcc,
// 			                	  "accName":recName,
// 			                	  "accType":"2",
// 			                	  "bankFullName":recBank,
// 			                	  "cnaps":recBankNo,
// 			                	  "remark":"测试",
// 			                	  "accType":1
// 			                	  "cnapsBankno":"308584000013"
// 			                	  }
								{	  
			                	  "seqId":"0",
			                	  "amount":8.08,
			                	  "accNo":"6226902107155295",
			                	  "accName":"中信 42213003",
			                	  "accType":"2",
			                	  "bankFullName":"中信银行",
			                	  "cnaps":"302222021121",
			                	  "remark":"测试"
			                	}
// 								{	  
// 			                	  "seqId":"1",
// 			                	  "amount":0.01,
// 			                	  "accNo":"6226900714309883",
// 			                	  "accName":"中信 43548528",
// 			                	  "accType":"2",
// 			                	  "bankFullName":"中信银行",
// 			                	  "cnaps":"302222021121",
// 			                	  "remark":"测试"
// 				                },
// 								{	  
// 			                	  "seqId":"2",
// 			                	  "amount":0.01,
// 			                	  "accNo":"6226901234309883",
// 			                	  "accName":"中信 56851605",
// 			                	  "accType":"2",
// 			                	  "bankFullName":"中信银行",
// 			                	  "cnaps":"302222021121",
// 			                	  "remark":"测试"
// 					             }
			                  ]
		}),
     	
     	contentType: "application/json",
     	success: function(result){ 
			alert("Yes!!!!!!!");     	
     	}
     });
}	

</script>

<a href="list.htm">返回公司账号列表</a><br/>
${errorMsg }
<c:if test="${empty errorMsg}">

<form action="edit.htm" method="post">
<input type="hidden" name="from_url_accNo" value="${from_url_accNo}"/>

<table border="0" cellpadding="0" cellspacing="0" style="width:100%">
  <tr>
      <th class="tablestyle_title" >【${account.bankFullName}】支付交易：</th>
  </tr>
  <tr>
    <td class="CPanel">
      <table border="0" cellpadding="0" cellspacing="0" style="width:100%">
        <tr>
          <td width="10%" align="right">付款人账号：</td>
          <td width="90%" ><input id="payAcc" name="payAcc" value="${account.accNo }" class="txt_input"/></td>
        </tr>
      	<tr>
      	  <td align="right">付款人名称：</td>
      	  <td><input id="payName" name="payName" value="${account.accName }" class="txt_input"/></td>
      	</tr>
        <tr>
          <td align="right">收款人账号：</td>
          <td><input id="recAcc" name="recAcc" class="txt_input"/></td>
        </tr>
        <tr>
          <td align="right">收款人名称：</td>
          <td><input id="recName" name="recName" value="" class="txt_input"/></td>
        </tr>
        <tr>
          <td align="right">收款人银行：</td>
          <td><input id="recBank" name="recBank" value="" class="txt_input"/></td>
        </tr>
        <tr>
          <td align="right">收款人联行号：</td>
          <td><input id="recBankNo" name="recBankNo" value="" class="txt_input"/></td>
        </tr>
        <tr>
          <td align="right">转账金额：</td>
          <td><input id="tranAmount" name="tranAmount" value="" class="txt_input"/></td>
        </tr>
        <tr>
          <td align="right">币别：</td>
          <td><select id="currency" name="currency"><option value="CNY">人民币</option></select></td>
        </tr>
        <tr>
          <td align="right">附言：</td>
          <td><input id="remark" name="remark" value="" class="txt_input"/></td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    
    <td align="center" colspan="2"><input type="button" value="提交" onclick="payMent()"/>
        <c:if test="${!empty input_error}">
	      <font color='red'>${input_error}</font>
        </c:if>
        ${success==true?"<font color='green'>提交成功!</font>":"" }
    </td>
<!--    <td></td>-->
<!--    <td></td>-->
  </tr>

</table>

</form>
</c:if>
</body>
</html>