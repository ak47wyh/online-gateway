<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>定时任务配置 - 银企直联中间件</title>
<link href="${pageContext.request.contextPath}/css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='${pageContext.request.contextPath}/js/jquery-1.7.1.js'></script>
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">

</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >定时任务列表 <a href="edit.htm">+添加定时配置</a></th>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <tr>
    <td>
      <table id="subtree1" style="DISPLAY: " width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td height="40" class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <tr bgcolor="#EEEEEE" style="text-align: center; font-weight: bold; ">
                      <td>定时配置ID</td>
                      <td>标题</td>
                      <td>定时类别</td>
                      <td>定时组名</td>
                      <td>定时名称</td>
                      <td>定时表达式</td>
                      <td>定时运行参数</td>
                      <td>状态</td>
                      <td>创建时间</td>
                      <td>修改时间</td>
                      <td>操作</td>
                    </tr>
                  <c:forEach items="${scheduleList}" var="schedule">                    
				    <tr bgcolor="#FFFFFF">
                      <td>${schedule['id']}</td>
                      <td>${schedule['title']}</td>
                      <td>${schedule['jobType']}</td>
                      <td>${schedule['jobGroup']}</td>
                      <td>${schedule['jobName']}</td>
                      <td>${schedule['cron']}</td>
                      <td>${schedule['params']}</td>
              		  <td>${schedule['status'] == 'enable'? '<font color="green">启用</font>' : '<font color="red">禁用</font>' }</td>
                      <td>${schedule['createTime']}</td>
                      <td>${schedule['updateTime']}</td>
                      <td>
                      <a href="edit.htm?id=${schedule['id']}">修改</a>
                       	<c:if test="${schedule['status'] != 'enable'}">
		              		<a href="enable.htm?id=${schedule['id']}" onclick="javascript:return confirm('确定启用该定时任务吗？')">启用</a>
		              	</c:if>
		              	<c:if test="${schedule['status'] == 'enable'}">
		              		<a href="disable.htm?id=${schedule['id']}" onclick="javascript:return confirm('确定禁用该定时任务吗？')">禁用</a>
		              	</c:if>
                      </td>
                    </tr>
                  </c:forEach>  
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
