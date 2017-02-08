<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>系统消息 - 银企直联中间件</title>
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
<link href="${pageContext.request.contextPath}/css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">

</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td>
      <table id="subtree1" style="DISPLAY: " width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td height="40" class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <tr class="CTitle" >
                      <td height="22" colspan="13" align="center" style="font-size:16px">系统消息列表</td>
                    </tr>
                    <tr bgcolor="#EEEEEE" style="text-align: center; font-weight: bold; ">
                      <td>消息ID</td>
                      <td>标题</td>
                      <td>内容</td>
                      <td>次数</td>
                      <td>创建时间</td>
                      <td>修改时间</td>
                      <td>操作</td>
                    </tr>
                  <c:forEach items="${messages}" var="msg">                    
				    <tr id='${msg.key}' name='system_msg_tr' bgcolor="#FFFFFF" style="color: ${msg.color};${msg.read ? '' : 'font-weight: bold;'}" >
				      <td>${msg.key}</td>
                      <td>${msg.title}</td>
                      <td>${msg.message}</td>
                      <td>${msg.count}</td>
                      <td><fmt:formatDate value="${msg.firstTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><fmt:formatDate value="${msg.lastTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td></td>
                    </tr>
                  </c:forEach>  
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
<script type="text/javascript">
	$.get('${pageContext.request.contextPath}/manage/message/receive-lastest.htm', function(result){});
	$('[name=system_msg_tr]').click(function(){
		$(this).css('font-weight','');
		var key = $(this).attr('id');
		$.get('${pageContext.request.contextPath}/manage/message/set-read.htm?key=' + key + '&t='+new Date().getTime(), function(result){});
	});
</script>
</body>
</html>
