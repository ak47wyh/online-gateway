<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>管理首页 - 银企直联中间件</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
<script type='text/javascript' src='${pageContext.request.contextPath}/js/common.js'></script>
</head>

<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="59" background="../../images/top.gif"><table width="99%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="1%"><a href="" target="_blank"><img src="../../images/logo.gif" width="557" height="59" border="0" /></a></td>
        <td width="64%" align="right" style="font-size:18px;vertical-align:top;">
        	欢迎你，<b>${sessionScope.user.realName}</b>
        	<a href="${pageContext.request.contextPath}/manage/user/password.htm" style="color:#0099FF;" target="mainFrame" >修改密码</a>
        	<a href="${pageContext.request.contextPath}/manage/message/list.htm" target="mainFrame" style="color:#0099FF;" id="system_msg">
        	<img id="system_msg_img" style="display: none;" src="../../images/newpm.gif" />系统消息(<span id="unreadSize">...</span>)</a> 
        	<a href="${pageContext.request.contextPath}/manage/user/logout.htm" onclick="parent.location = this.href;">退出</a>
        </td>
      </tr>
    </table></td>
  </tr>
</table>
<script type="text/javascript">
function getUnreadSize(){
	$.get('${pageContext.request.contextPath}/manage/message/unread-count.htm', function(result){
		if(result.unreadSize > 0){
			$('#system_msg').css({'font-weight': 'bold'});
			$('#system_msg_img').show();
			$('#unreadSize').text(result.unreadSize);
		}else{
			$('#system_msg').css('font-weight','');
			$('#system_msg_img').hide();
			$('#unreadSize').text(result.unreadSize);
		}
		$.WindowMsg.clear();
		if(result.hasLastestMessage == 'true'){
			$.WindowMsg.show('系统消息', '有新的系统消息');
		}
	});
}
getUnreadSize();
setInterval('getUnreadSize()', 5 * 1000);
</script>
</body>
</html>
