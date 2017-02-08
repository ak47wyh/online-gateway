<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>交易账户列表</title>
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
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
<link href="../../css/jquery-ui.css" rel="stylesheet" type="text/css"/>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<script type='text/javascript' src='../../js/jquery-ui-datepicker.js'></script>
</head>

<body>

<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >交易账户列表 <a href="add.htm">+添加交易账户</a></th>
  </tr>
  <tr>
      <td>  	
      <form action="list.htm" method="get">
	  <table style="margin:0 auto; ">
	  	<tr>
	  		<td style="width: 13%">通道账户名称：</td>
	  		<td><input id="transDate" name="appCode" class="textInput" value="${appCode}"/></td>
	  		<td style="width: 13%">交易商户号：</td>
		    <td><input id="transDate" name="payMerchantNo" class="textInput" value="${payMerchantNo}"/></td>
	  	</tr>
	  	<tr><td></td><td colspan="2"><input type="submit" value="查询" style="width: 50px; margin-left: 200px;"/></td><td></td></tr>
	  </table>    
	  </form>
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
                      <td width="10%">通道账号名称</td>
                      <td width="10%">AppId</td>
                      <td width="10%">subAppId</td>
                      <td width="15%">交易商户名称</td>
                      <td width="15%">交易商户号</td>
                      <td width="15%">交易子商户号</td>
                      <td width="15%">交易密钥</td>
                      <td width="10%">操作</td>
                    </thead>
                  <c:forEach items="${pageBean.result}" var="bp">                    
				    <tr bgcolor="#FFFFFF">
				      <td>${bp.appCode}</td>
				      <td>${bp.appId }</td>
				      <td>${bp.subAppId }</td>
                      <td>${bp.payMerchantName }</td>
					  <td>${bp.payMerchantNo }</td>
                      <td>${bp.payMerchantSubNo}</td>
                      <td>${bp.payMerchantKey }</td>
                      <td>
                      	<a href="edit.htm?id=${bp.id}">编辑</a>
                      	<a href="delete.htm?id=${bp.id}" onclick="javascript:return confirm('确定删除该交易账号吗？')">删除</a>
                      </td>
                    </tr>
                  </c:forEach>  
                  <tr>
                  	<td colspan="5">
                  		<%@include file="../pager.jsp"%>
                  	</td>
                  </tr>
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
</body>
</html>
