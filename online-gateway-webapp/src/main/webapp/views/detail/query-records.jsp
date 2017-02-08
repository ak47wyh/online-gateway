<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>查询历史记录 - 银企直联中间件</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
.textInput{
	width: 180px;
}
html { overflow-x: auto; overflow-y: auto; border:0;} 
-->
</style>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
<link href="../../css/jquery-ui.css" rel="stylesheet" type="text/css"/>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<script type='text/javascript' src='../../js/common.js'></script>
<script type='text/javascript' src='../../js/jquery-ui-datepicker.js'></script>
<script>
$(function(){
	$("#beginDate").datepicker();
	$("#endDate").datepicker();
	$('#ui-datepicker-div').hide();//jquery ui bug????
});
</script>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >交易明细查询记录</th>
  </tr>
  <tr><td>
  	<form action="query-records.htm" method="get">
	  <table width="800px" style="margin:0 auto; ">
	  	<tr><td>公司账号：</td><td colspan="3"><select id="companyAccSelect" name="accNo"></select></td></tr>
	  	<tr>
	  		<td style="width: 13%">明细日期：</td>
	  		<td><input name="beginDate" id="beginDate" class="textInput" value="${param.beginDate}"/></td>
	  		<td style="width: 13%">——</td>
	  		<td><input name="endDate" id="endDate" class="textInput" value="${param.endDate}"/></td>
	  	</tr>
	  	<tr><td></td><td colspan="2"><input type="submit" value="查询" style="width: 50px; margin-left: 200px;" onclick="queryDetail(); return false;"/><span id="loadFlag" style="display: none;">加载中...</span></td><td></td></tr>
	  </table>    
	  </form>
  </td></tr>
  <tr>
    <td>
      <table id="subtree1" width="95%" border="0" cellspacing="0" cellpadding="0" id="detail_table">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <thead style="background-color: #EEEEEE; font-weight: bold; text-align: center;" id="details">
	                  <td>ID</td>
                      <td>公司账号</td>
                      <td>明细日期</td>
                      <td>查询类型</td>
                      <td>创建时间</td>
                      <td>修改时间</td>
                    </thead>
                    <c:forEach items="${pageBean.result}" var="queryRecord">
                    <tr bgcolor="#FFFFFF">
	                    <td>${queryRecord.id}</td>
	                    <td>${queryRecord.accNo}</td>
	                    <td><fmt:formatDate value="${queryRecord.detailDay}" pattern="yyyy-MM-dd"/></td>
	                    <td>${queryRecord.type == 0 ? '<font color="green">当日明细</font>' : queryRecord.type == 1 ? '历史明细' : '【未知类型】'}</td>
	                    <td><fmt:formatDate value="${queryRecord.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
	                    <td><fmt:formatDate value="${queryRecord.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                    </tr>
                    </c:forEach>
                  <tr>
                  	<td colspan="7">
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
