<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>管理首页 - 银企直联中间件</title>
<link href="../css/manage.css" media="screen" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../js/jquery-1.7.1.js'></script>
</head>
<body>
<b>管理：</b>
<a href="frontend/list.htm" target="_blank">前置机管理</a>
<a href="account/list.htm" target="_blank">公司账号管理</a>
<a href="config/list.htm" target="_blank">参数管理</a>
<br/>
<b>系统：</b>
<a href="system/stat.htm" target="_blank">运行统计</a>
<a href="system/init.htm" target="_blank">系统初始化</a>
<br/>
<b>业务测试：</b> 
<a href="../manage/account/list.htm" target="_blank">余额</a>
<!--<a href="../manage/payment/list.htm" target="_blank" onclick="alert('敬请期待');return false;">支付</a>-->
<a href="../manage/payment/list.htm" target="_blank" >支付</a>
<a href="#" target="_blank" onclick="alert('敬请期待');return false;">支付查询</a>
<hr/>
<h3>当前银行列表：</h3>
<table border="1" class="tb_line">
<thead>
	<tr><th>银行简称</th><th>银行全称</th><th>余额接口实现</th><th>支付接口实现</th></tr>
</thead>
<c:forEach items="${bankProfiles}" var="bankProfile" varStatus="bst">
<tr>
<td>${bankProfile.bankName}</td>
<td>${bankProfile.bankFullName}</td>
<td>
${bankTransMap[bankProfile]['balance'][0].code }（${bankTransMap[bankProfile]['balance'][0].desc }）
</td>
<td>
<c:forEach items="${bankTransMap[bankProfile]['pay']}" var="pay" varStatus="st">
	<a title="${pay.navigationInfo}" href="#" onclick="$('#pay_${bst.count}_${st.count}').toggle();return false;">
	${pay.code }（${pay.desc }）
	</a>
	<ul id="pay_${bst.count}_${st.count}" style="margin-left: 0px; display: none; background-color: #F1F1F1; font-size: 12px; border: 1px dotted;">
	<li style="list-style-type:none;"><b>接口描述：</b>${pay.desc}</li>
	<li style="list-style-type:none;"><b>接口支持：</b>${pay.navigationInfo}</li>
	<c:if test="${!empty pay.queryCode }">
	<li style="list-style-type:none;"><b>状态查询：</b>${pay.queryCode }（${pay.queryDesc }）</li>
	</c:if>
	</ul>
	${st.count >= 1 ? "<br/>" : ""}
</c:forEach>
</td>
</tr>
</c:forEach>
</table>
</body>
</html>
