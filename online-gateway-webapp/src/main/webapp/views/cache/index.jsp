<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>缓存管理 - 银企直联中间件</title>
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
</head>
<body>
<div>
本地缓存Key：
<input id="localCacheInput" style="width: 300px"/>
<input value="查看" id="localCacheGet" type="button"  onclick="get(true, $('#localCacheInput').val())"/>
<input value="删除" id="localCacheDelete" type="button" onclick="del(true, $('#localCacheInput').val())"/>
<div id="localCacheContent" style="display: none;">
</div>
</div>
<div style="margin-top: 200px;">
memcache缓存Key：
<input id="remoteCacheInput" style="width: 300px"/>
<input value="查看" id="remoteCacheGet" type="button" onclick="get(false, $('#remoteCacheInput').val())"/>
<input value="删除" id="remoteCacheDelete" type="button" onclick="del(false, $('#remoteCacheInput').val())"/>
<div id="remoteCacheContent" style="display: none;">
</div>
</div>
<script type="text/javascript">
function get(local, key){
	var contentDiv = local ? $('#localCacheContent') : $('#remoteCacheContent');
	contentDiv.html('');
	contentDiv.hide();
	$.get('${pageContext.request.contextPath}/manage/cache/get.htm?local=' + local + '&key=' + key,function(result){
		var content = result.object ? '<font color="green">' + result.object + '</font>' : '<font color="red">【缓存对象不存在】</font>';
		if(local){
			contentDiv.html(content);
		}else{
			contentDiv.html(content);
		}
		contentDiv.show();
	});
}
function del(local, key){
	var contentDiv = local ? $('#localCacheContent') : $('#remoteCacheContent');
		contentDiv.html('');
		contentDiv.hide();
	$.get('${pageContext.request.contextPath}/manage/cache/delete.htm?local=' + local + '&key=' + key,function(result){
		var content = result.success ? '<font color="green">删除成功</font>' : '<font color="red">删除失败(可能缓存对象不存在)</font>';
		if(local){
			$('#localCacheContent').html(content);
		}else{
			$('#remoteCacheContent').html(content);
		}
		contentDiv.show();
	});
}
</script>
</body>
</html>
