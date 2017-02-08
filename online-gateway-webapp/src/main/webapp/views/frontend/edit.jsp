<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>添加前置机配置 - 银企直联中间件</title>
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
<script>
function noticeSubmitting(fieldName){
//	$('#tips').css({'color' : 'black'}).html('正在上传文件...');
//	$('#status').val('');
}
function noticeSuccess(fieldName, fileName){
	$('[name='+fieldName+']').val(fileName);
}
function noticeError(fieldName, msg){
	alert('上传失败：'+msg);
}
function noticeDelete(fieldName){
	$('[name='+fieldName+']').val('');
}
</script>
</head>
<body>
<a href="list.htm">返回前置机列表</a><br/>
${errorMsg }
<c:if test="${empty errorMsg}">
【${frontEndConfig.bankFullName}】前置机配置：
<form action="edit.htm?id=${param.id }" method="post">
<input type="hidden" name="frontend_id" value="${frontEndConfig.id == 0 ? "" : frontEndConfig.id}"/>
<input type="hidden" name="frontend_bankName" value="${frontEndConfig.bankName}"/>
<table>
<thead>
<tr><th>配置项</th><th>配置值</th><th>配置说明</th></tr>
<thead>
<tr><td  style="border-bottom:1pt solid black;" colspan="3"></td></tr>
<tr><td>name</td><td><input name="frontend_name" value="${frontEndConfig.name }"/></td><td>前置机配置名称</td></tr>
<c:forEach items="${frontEndConfig.allPropertys}" var="property">
	<tr>
	<td>${property.name}</td>
	<td>	
		<c:if test="${not property.file}">
		<input name="${property.name}" value="${property.val}" ${property.readOnly ? "readonly='readonly'" : ""}/>
		</c:if>
		<c:if test="${property.file}">
			<c:set value="${property.val}" var="fileName"/>
			<input name="${property.name}" value="${property.val}" type="hidden"/>
			<iframe style="width: 300px; height: 28px; border: 0px;" frameborder="0" scrolling="no" 
					src="${pageContext.request.contextPath}/manage/file/file-upload.htm?fieldName=${property.name}&fileName=<%=pageContext.getAttribute("fileName")==null?"":java.net.URLEncoder.encode(java.net.URLEncoder.encode(pageContext.getAttribute("fileName").toString() , "UTF-8") , "UTF-8") %>">
			</iframe>
		</c:if>
	</td>
	<td>${property.desc}</td>
	</tr>
</c:forEach>
<tr><td></td><td><input type="submit" value="提交"/>${success==true?"<font color='green'>提交成功!</font>":"" }</td><td></td></tr>
</table>
</form>
</c:if>
</body>
</html>
