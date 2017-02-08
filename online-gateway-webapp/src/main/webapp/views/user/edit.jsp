<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>用户编辑 - 银企直联中间件</title>
</head>
<body>
<div style="width: 600px; height: 400; margin: 0 auto;">
<form action="edit.htm?user_name=${user_name}" method="post">
<table>
	<tr><td>用户名：</td><td><input type="text" name="name" /></td></tr>
	<tr><td>真实名字：</td><td><input type="text" name="realName" /></td></tr>
	<tr><td>密码：</td><td><input type="password" name="password" /></td></tr>
	<tr><td><input type="submit" value="提交"/></td><td>${message}</td></tr>
</table>
</form>
</div>
</body>
</html>
