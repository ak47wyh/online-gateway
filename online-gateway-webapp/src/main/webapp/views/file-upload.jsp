<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script src="${pageContext.request.contextPath}/js/jquery-1.7.1.js"></script>
<title>文件上传</title>
<script>
function processForm(){
	var formButton = $('#formButton');
	if(formButton.val() == '上传'){
		if($('[name=file]').val() == ''){
			alert('请选择文件!');
			return false;
		}
		parent.noticeSubmitting('${fieldName}');
		return true;
	} else {
		$('#fileInput').show();
		$('#fileDisplay').hide();
		$('#formDelete').hide();
		$('#formCancel').show();
		formButton.val('上传');
		return false;
	}
}
function cancelUpload(){
	$('#fileInput').hide();
	$('#fileDisplay').show();
	$('#formButton').val('重传').show();
	$('#formCancel').hide();
}
function confirmDelete(){
	if(confirm('确定删除该文件吗？')){
		parent.noticeDelete('${fieldName}');
		return true;
	}
}
<c:if test="${not empty errorMsg}">
	parent.noticeError('${fieldName}', '${errorMsg}');
</c:if>
<c:if test="${success && empty errorMsg}">
	parent.noticeSuccess('${fieldName}', '${fileName}');
</c:if>
</script>
</head>
<body style="margin: 0px auto; padding: 0px;">
<form action="file-upload.htm?${queryString}" method="post" enctype="multipart/form-data" onsubmit="return processForm();">
	<input id="fileInput" style="width: 180px;${empty fileName ? '' : 'display: none;'}" type="file" name="file" value="" />
	<span id="fileDisplay" style="${empty fileName ? 'display: none;' : ''}" title="${fileName}">${fn:substring(fileName, 0, 13)}${fn:length(fileName) > 15 ? '...' : ''}</span>
	<a id="formDelete" style="${empty fileName ? 'display: none;' : ''}" 
			href="file-delete.htm?fileName=<%=request.getAttribute("fileName")==null?"":java.net.URLEncoder.encode(java.net.URLEncoder.encode(request.getAttribute("fileName").toString() , "UTF-8") , "UTF-8") %>" onclick="return confirmDelete();">删除</a>
	<input id="formButton" style="padding-left: 2px;" type="submit" value="${empty fileName ? '上传' : '重传'}"/>
	<input id="formCancel" style="display: none;" type="button" value="取消" onclick="cancelUpload()"/>
</form>
</body>
</html>
