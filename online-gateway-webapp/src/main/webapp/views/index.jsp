<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>管理首页 - Online-无卡支付网关-后台管理界面</title>
<%
String path = request.getRequestURI();
String baseUrl = request.getContextPath() + "/" ;
%>

</head>
  <frameset rows="59,*" cols="*" frameborder="no" border="0" framespacing="0">
<!--  <frame src="files/top.html" name="topFrame" scrolling="No" noresize="noresize" id="topFrame" title="topFrame" />-->
    <frame src="../views/files/top.jsp" name="topFrame"  noresize="noresize" id="topFrame" title="topFrame" />
    <frameset cols="213,*" frameborder="no" border="0" framespacing="0">
<!--    <frame src="files/left.html" name="leftFrame" scrolling="No" noresize="noresize" id="leftFrame" title="leftFrame" />-->
      <frame src="../views/files/left.jsp" name="leftFrame" noresize="noresize" id="leftFrame" title="leftFrame" />
      <frame src="../views/files/mainfra.jsp" name="mainFrame" id="mainFrame" title="mainFrame" />
    </frameset>
  </frameset>
  <noframes>
    <body>
    </body>
  </noframes>
</html>
