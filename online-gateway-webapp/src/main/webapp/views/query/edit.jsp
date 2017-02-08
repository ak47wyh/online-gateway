<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号编辑 - 银企直联中间件</title>
<style>
.txt_input {
	width: 100%;
}
</style>
</head>
<body>
<a href="list.htm">返回公司账号列表</a><br/>
${errorMsg }
<c:if test="${empty errorMsg}">
【${account.bankFullName}】公司账号配置：
<form action="edit.htm" method="post">
<input type="hidden" name="from_url_accNo" value="${from_url_accNo}"/>
<table style="width: 1000px">
<thead>
<tr><th>账户属性</th><th width="50%">配置值</th></tr>
</thead>
<tr><td style="border-bottom:1pt solid black;" colspan="2"></td></tr>
<tr><td>银行简码：</td><td><input name="bankName" value="${account.bankName }" readonly="readonly" class="txt_input"/></td></tr>
<tr><td>银行全称：</td><td><input name="bankFullName" value="${account.bankFullName }" readonly="readonly" class="txt_input"/></td></tr>
<tr><td>账号：</td><td><input name="accNo" value="${account.accNo }" class="txt_input"/></td></tr>
<tr><td>户名：</td><td><input name="accName" value="${account.accName }" class="txt_input"/></td></tr>
<tr><td>支行名称：</td><td><input name="bankBranchName" value="${account.bankBranchName }" class="txt_input"/></td></tr>
<tr><td>开户地区号：</td><td><input name="areaCode" value="${account.realAreaCode }" class="txt_input"/></td></tr>
<tr><td>联行号：</td><td><input name="cnaps" value="${account.cnaps }" class="txt_input"/></td></tr>
<tr><td>设置为该银行默认账号（如果设置了当前为默认，则该银行其他账号自动变为非默认）：</td><td><input id="bankDefault" type="checkbox" name="bankDefault" value="true" ${account.bankDefault?"checked='checked'":"" }><label for="bankDefault">设为默认</label> </td></tr>
<tr><td>币别：</td><td><select name="currency"><option value="CNY">人民币</option></select></td></tr>
<c:if test="${fn:length(account.extPropertys) > 0}">
<tr><td colspan="2" style="border-bottom:1pt solid black; font-weight: bold;">账户扩展属性：</td></tr>
<c:forEach items="${account.extPropertys}" var="property">
	<tr><td>${property.desc}：</td><td><input name="ext_${property.name}" value="${property.val}" ${property.readOnly ? "readonly='readonly'" : ""} class="txt_input"/></td></tr>
</c:forEach>
</c:if>
<tr><td></td><td><input type="submit" value="提交"/>
<c:if test="${!empty input_error}">
	<font color='red'>${input_error}</font>
</c:if>
${success==true?"<font color='green'>提交成功!</font>":"" }
</td><td></td></tr>
</table>
</form>
</c:if>
</body>
</html>
