<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>前置机列表 - 银企直联中间件</title>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
<script><!--
	var queryString = window.location.search;
	var regExp = new RegExp('.*msg=([^&/]+).*', "i");
	var msg = queryString.replace(regExp, '$1');
	if($.trim(msg) == 'binding'){
		alert('该前置机存在与账号绑定关系，请先解除绑定');
		window.location = 'list.htm';
	}
/**
	$(document).ready(function(){
		$('.data_tr').mouseover(
			function(){$(this).css('bgcolor', '#EEEEEE');}
		).mouseout(
			function(){$(this).css('bgcolor', '#FFFFFF');}
		);
	}
**/
</script>
</head>

<body>
添加前置机：
<c:forEach items="${bankProfiles}" var="bankProfile">
	<a href="edit.htm?bankName=${bankProfile.bankName}">${bankProfile.bankFullName}</a>
</c:forEach>
<hr/>
<!--<h3>前置机列表：</h3>-->
<!--<table border="1" class="tb_line">-->
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
  <td>
    <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td height="40" class="font42">
          <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
            <tr class="CTitle" >
              <td height="22" colspan="13" align="center" style="font-size:16px">前置机列表</td>
            </tr>
            <tr bgcolor="#EEEEEE">
              <td>前置机配置ID</td>
              <td>配置名称</td>
              <td>银行名称</td>
              <td>银行全称</td>
              <td>前置机IP</td>
              <td>前置机端口</td>
              <td>并发数</td>
              <td>状态</td>
              <td>操作</td>
            </tr>
          <c:forEach items="${list}" var="fe">                    
			<tr bgcolor="#FFFFFF" class="data_tr">
  			  <td height="20">${fe.id }</td>
              <td >${fe.name }</td>
			  <td>${fe.bankName }</td>
              <td>${fe.bankFullName }</td>
              <td>${fe.ip.val }</td>
              <td>${fe.port.val }</td>
              <td>${fe.totalConcurrentNum.val }</td>
              <td>${fe.enable ? '<font color="green">启用</font>' : '<font color="red">禁用</font>' }</td>
              <td>
              	<a href="edit.htm?id=${fe.id}">编辑</a>
              	<a href="delete.htm?id=${fe.id}" onclick="javascript:return confirm('确定删除该前置机吗？')">删除</a>
              	<c:if test="${not fe.enable}">
              		<a href="enable.htm?id=${fe.id}" onclick="javascript:return confirm('确定启用该前置机吗？')">启用</a>
              	</c:if>
              	<c:if test="${fe.enable}">
              		<a href="disable.htm?id=${fe.id}" onclick="javascript:return confirm('确定禁用该前置机吗？')">禁用</a>
              	</c:if>
              	</td>
            </tr>
          </c:forEach>  
          </table>
        </td>              
      </tr>
    </table>
  </td>
</tr>
</table>
</body>
</html>
