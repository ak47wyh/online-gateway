<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="no-js" lang="zh-CN">
<head>
<%   
String path = request.getContextPath();   
String basePath = request.getScheme()+"://" +request.getServerName()+":" +request.getServerPort()+path+"/" ;   
%>   
<title>H5Pay Test</title>
<meta charset="UTF-8" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1" />
<meta name="robots" content="noindex, nofollow" />
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1" />
<style type="text/css">
body {
	text-align:center
} 
</style>


</head>
<body>
<h2>H5 发起付款支付请求页面</h2>
<form action="<%=basePath %>/wechat/H5payJs.do" method="post">
预支付交易会话标识:<input name="prepay_id" type="text" value="" size="32"><br/><br/>
<input name="submit" type="submit"><br>
</form>
</body>
</html>
