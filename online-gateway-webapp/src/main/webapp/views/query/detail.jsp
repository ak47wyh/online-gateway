<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>交易详情 - 银企直联中间件</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
.tabfont01 {	
	font-family: "宋体";
	font-size: 9px;
	color: #555555;
	text-decoration: none;
	text-align: center;
}
.font051 {font-family: "宋体";
	font-size: 12px;
	color: #333333;
	text-decoration: none;
	line-height: 20px;
}
.font051 {font-family: "宋体";
	font-size: 12px;
	color: #FF0000;
	text-decoration: none;
}
.button {
	font-family: "宋体";
	font-size: 14px;
	height: 37px;
}
html { overflow-x: auto; overflow-y: auto; border:0;} 
-->
</style>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">
var bankName = "${param.bankName}";
function queryPayments(batchSeqId, appCode){
    $.ajax({
     	type: "POST",
     	url:"${pageContext.request.contextPath}/services/online/query", 
     	dataType:"json",
     	data:JSON.stringify({
			"batchSeqId" : batchSeqId,
			"appCode" : appCode
			}),
     	contentType: "application/json",
     	success: function(result){ 
     		if(result.status = 'success'){
     			location.reload();
     		}else{
     			alert(result.statusMsg);
     		}
     	}
     });
}
</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="40px">
	  	<a href="list.htm" style="margin-left: 50px;">返回批次列表</a><input type="button" value="同步交易状态" onclick="queryPayments('${param.batchSeqId}', '${param.accNo}')"/>	
	  	<span>待处理：${init};&nbsp;正在提交：${processing};&nbsp;已提交：${submmited};&nbsp;<font color="green">交易成功：${success}</font>;&nbsp;<font color="red">交易失败：${fail};</font>&nbsp;交易撤消：${cancle};&nbsp;其他状态：${other}
	  	</span>
    </td>
  </tr>
  <tr>
    <td>
      <table id="subtree1" style="DISPLAY: " width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <thead style="background-color: #EEEEEE; font-weight: bold; text-align: center;">
                      <td>ID</td>
                      <td>批次号</td>
                      <td>银行名称</td>
                      <td>公司账号（出款账号）</td>
                      <td>客户银行</td>
                      <td>客户银行全名</td>
                      <td>客户账号</td>
                      <td>客户户名</td>
                      <td>账户类型</td>
                      <td>金额</td>
                      <td>联行号</td>
                      <td>提交银行时间</td>
                      <td>状态</td>
                      <td>状态信息</td>
                      <td>支付返回状态</td>
                      <td>支付返回状态信息</td>
                      <td>最新返回状态</td>
                      <td>最新返回状态信息</td>
                      <td>向银行同步次数</td>
                      <td width="3%">操作</td>
                    </thead>
                  <c:forEach items="${payments}" var="p">                    
				    <tr bgcolor="#FFFFFF">
				      <td>${p.id}</td>
				      <td>${p.batchSeqId}</td>
				      <td>${p.bankName}</td>
				      <td>${p.accNo}</td>
				      <td>${p.customerBankName}</td>
				      <td>${p.customerBankFullName}</td>
				      <td>${p.customerAccNo}</td>
				      <td>${p.customerAccName}</td>
				      <td>${p.customerAccType}</td>
				      <td>${p.amount}</td>
				      <td>${p.customerCnaps}</td>
				      <td><fmt:formatDate value="${p.submitPayTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
				      <td>${p.status}</td>
				      <td>
				      <c:if test="${empty p.statusMsg}">
				         <c:if test="${p.status==0 }">待处理</c:if>
				         <c:if test="${p.status==10 }">正在提交</c:if>
				         <c:if test="${p.status==20 }">已提交</c:if>
				         <c:if test="${p.status==25 }">等待支付</c:if>
				         <c:if test="${p.status==30 }">交易成功</c:if>
				         <c:if test="${p.status==35 }">交易撤消</c:if>
				         <c:if test="${p.status==40 }">交易失败</c:if>
				         <c:if test="${p.status==50 }">未确定</c:if>
				         <c:if test="${p.status==60 }">交易退款中</c:if>
				         <c:if test="${p.status==65 }">退款失败</c:if>
				         <c:if test="${p.status==70 }">退款成功</c:if>
				         <c:if test="${p.status==80 }">冲正</c:if>
				         <c:if test="${p.status==85 }">冲正失败</c:if>
				         <c:if test="${p.status==99 }">关闭失败</c:if>
				         <c:if test="${p.status==100 }">交易关闭</c:if>
				      </c:if>
                      <c:if test="${not empty p.statusMsg}">${p.statusMsg }</c:if>
				      </td>
				      <td>${p.payBankStatus}</td>
				      <td>${p.payBankStatusMsg}</td>
				      <td>${p.bankStatus}</td>
				      <td>${p.bankStatusMsg}</td>
				      <td>${p.queryTransCount}</td>
                      <td></td>
                    </tr>
                  </c:forEach>  
                    <tfoot style="background-color: bisque;">
                        <tr>
                            <td colspan="20">
                                <c:if test="${not empty priorPageHref}"><a href="${priorPageHref}" >上一页</a></c:if>
                                <c:if test="${empty priorPageHref}"><span>上一页</span></c:if>
                                <span style="padding: 5px;"></span>
                                <c:if test="${not empty nextPageHref}"><a href="${nextPageHref}">下一页</a></c:if>
                                <c:if test="${empty nextPageHref}"><span>下一页</span></c:if>
                                <span style="padding-left: 10px;">当前页：${pageNo}</span>
                                <span style="padding-left: 10px;">总页数：${totalPage}</span>
                             </td>
                        </tr>
                    </tfoot>
                  </table>
                </td>              
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<center>------------- THE END --------------</center>
</body>
</html>
