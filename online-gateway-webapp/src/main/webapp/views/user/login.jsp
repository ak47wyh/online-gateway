<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>用户登陆  - Online-无卡支付网关</title>
<%
String path = request.getRequestURI();
String baseUrl = request.getContextPath() + "/" ;
%>
</head>
<body>
<div style="width: 600px; height: 400; margin: 0 auto;">
<form action="login.htm?redirectURL=${redirectURL}" method="post">
	<table>
		<tr><td>用户名：</td><td><input type="text" name="name" value="${param.name}"/></td></tr>
		<tr><td>密码：</td><td><input type="password" name="password"/></td></tr>
		<tr><td><input type="submit" value="登陆"/></td><td>${message}</td></tr>
	</table>
</form>
</div>
</body>
</html>
