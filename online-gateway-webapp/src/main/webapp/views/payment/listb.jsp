<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号列表 - 银企直联中间件</title>
<link href="../../css/manage.css" media="screen" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
</head>
<body>
<script type="text/javascript">
function query_balance(accNo){
	$('#bal_q_' + accNo).html('查询中...');
  $.get("balance.htm",{"accNo": accNo}, function(result){
  	  eval('result=' + result);
  	  if(result.status == 'success'){
  	  	$('#bal_' + accNo).html('<font color="green">'+result.balance+'</font>');
  	  }else{
  	  	alert(result.statusMsg);
  	  }
  	  	$('#bal_q_' + accNo).html('查询');
  });
}	
</script>
<b>添加账号：</b>
<c:forEach items="${bankProfiles}" var="bankProfile">
	<a href="edit.htm?bankName=${bankProfile.bankName}">${bankProfile.bankFullName}</a>
</c:forEach>
<hr/>
<h3>账号列表：</h3>
<table border="1" class="tb_line">
<thead>
	<tr><th>账号</th><th>户名</th><th>银行名称</th><th>银行全称</th><th>支行名称</th><th>地区码</th><th>联行号</th><th>币别</th><th>该银行默认账号</th><th>业务操作</th><th>创建时间</th><th>修改时间</th><th>操作</th></tr>
</thead>
<c:forEach items="${list}" var="acc">
<tr>
	<td>${acc.accNo }</td>
	<td>${acc.accName }</td>
	<td>${acc.bankName }</td>
	<td>${acc.bankFullName }</td>
	<td>${acc.bankBranchName }</td>
	<td>${acc.areaCode }</td>
	<td>${acc.cnaps }</td>
	<td>${acc.currency }</td>
	<td>${acc.bankDefault==true?"是":"否" }</td>
<!--	<td>【余额：<span id="bal_${acc.accNo}">N/A</span>】<a id="bal_q_${acc.accNo}" href="#" onclick="query_balance('${acc.accNo}')">查询</a></td>-->
<!--	<a href="pay.htm?accNo=${acc.accNo}">转账</a>-->
	<td><a href="pay.htm?accNo=${acc.accNo }">转账</a></td>
	<td><fmt:formatDate value="${acc.createTime }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
	<td><fmt:formatDate value="${acc.updateTime }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
	<td><a href="edit.htm?accNo=${acc.accNo }">编辑</a>&nbsp;<a href="delete.htm?accNo=${acc.accNo}" onclick="javascript:return confirm('确定删除该账号吗？')">删除</a></td>
</tr>
</c:forEach>
</table>

</body>
</html>
