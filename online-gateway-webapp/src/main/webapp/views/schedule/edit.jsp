<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>定时任务编辑 - 银企直联中间件</title>
<style>
.txt_input {
	width: 100%;
}
.txt_input_array {
	width: 80%;
}
</style>
<link href="${pageContext.request.contextPath}/css/css.css" rel="stylesheet" type="text/css" />
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
</head>
<body>
<script>
var map = {};
<c:forEach items="${scheduleJobs}" var="scheduleJob">
map.${scheduleJob.jobType} = {"jobType": "${scheduleJob.jobType}","title": "${fn:replace(scheduleJob.title, "\"", "\\\"")}","jobGroup": "${scheduleJob.jobGroup}","jobName": "${scheduleJob.jobName}","configDesc": "${fn:replace(scheduleJob.configDesc, "\"", "\\\"")}"}; 
</c:forEach>
function whenJobTypeSelect(){
	var jobType = $('[name=jobType]').val();
	var scheduleJob = map[jobType];
	$('[name=jobType]').val(scheduleJob.jobType);
	$('[name=jobGroup]').val(scheduleJob.jobGroup);
	$('[name=jobName]').val(scheduleJob.jobName);
	$('[name=configDesc]').val(scheduleJob.configDesc);
}
$(document).ready(function(){
	$('[name=jobType]').change(function(){
		whenJobTypeSelect();
	});
	whenJobTypeSelect();
});
</script>
<a href="list.htm">返回定时任务配置</a><br/>
<form action="edit.htm" method="post">
<input type="hidden" name="id" value="${schedule.id}"/>
<table style="width: 1000px">
<tr><td>定时类别：</td><td>
<select name="jobType" ${empty schedule.jobType ? '' : 'disabled="disabled"'}>
	<c:forEach items="${scheduleJobs}" var="scheduleJob">
		<option value="${scheduleJob.jobType}" ${schedule.jobType == scheduleJob.jobType ? 'selected="selected"' : ''}>${scheduleJob.title }</option>
	</c:forEach>
</select>
</td></tr>
<tr><td>定时组名：</td><td><input name="jobGroup" value="${schedule.jobGroup }" readonly="readonly" disabled="disabled" class="txt_input"/></td></tr>
<tr><td>定时名称：</td><td><input name="jobName" value="${schedule.jobName }" readonly="readonly" disabled="disabled" class="txt_input"/></td></tr>
<tr><td>定时表达式：</td><td><input name="cron" value="${schedule.cron }" class="txt_input"/>（定时表达式有两种方式：1. “f:60s”表示每60秒执行一次，单位有“s,m,h”代表秒分时；2. 使用cron表达式）</td></tr>
<tr><td>定时运行参数：</td>
<td><textarea name="params" rows="8" class="txt_input">${schedule.params }</textarea></td>
</tr>
<tr><td>配置说明：</td><td><textarea name="configDesc" rows="8" class="txt_input" readonly="readonly" disabled="disabled"></textarea>
</td>
</tr>
<tr><td></td><td><input type="submit" value="提交"/>
<c:if test="${!empty input_error}">
	<font color='red'>${input_error}</font>
</c:if>
${success==true?"<font color='green'>提交成功!</font>":"" }
</td><td></td></tr>
</table>
</form>
</body>
</html>
