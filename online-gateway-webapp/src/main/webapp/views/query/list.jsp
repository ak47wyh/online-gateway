<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>支付查询 - 银企直联中间件</title>
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
<script type="text/javascript">
var batchIds = [];
$.ajaxSetup({ 
    async : false 
});
$(function(){
	$("#transDate").datepicker();
	$('#ui-datepicker-div').hide();//jquery ui bug????
});
function refreshStatus(batchSeqId){
	$.get('stat-status.htm?batchSeqId=' + batchSeqId + '&t=' + new Date().getTime(), function(result){
  	  $('#init_' + batchSeqId).html(result.init);
  	  $('#processing_' + batchSeqId).html(result.processing);
  	  $('#submmited_' + batchSeqId).html(result.submmited);
  	  $('#success_' + batchSeqId).html(result.success);
  	  $('#fail_' + batchSeqId).html(result.fail);
  	  $('#cancle_' + batchSeqId).html(result.cancle);
  	  $('#other_' + batchSeqId).html(result.other);
	});
}
var accounts = [];
<c:forEach items="${accounts}" var="account">
accounts.push({'bankName' : '${account.bankName}', 'accNo' : '${account.accNo}', 'accName': '${account.accName}'});
</c:forEach>
$(document).ready(function(){
	var accNoSelect = $('[name=accNo]');
	accNoSelect.empty();
	accNoSelect.append('<option value="">---------------------请选择账号-------------------</option>');
	for(var i=0; i<accounts.length; i++){
		if(accounts[i].bankName == '${param.bankName}')
			accNoSelect.append('<option value="'+accounts[i].accNo+'" '+(accounts[i].accNo == '${param.accNo}' ? 'selected="selected"' : '')+'>'+accounts[i].accNo+' - '+ accounts[i].accName+'</option>');
	}
	$('[name=bankName]').change(function(){
		var bankName = $(this).val();
		var accNoSelect = $('[name=accNo]');
		accNoSelect.empty();
		accNoSelect.append('<option value="">---------------------请选择账号-------------------</option>');
		for(var i=0; i<accounts.length; i++){
			if(accounts[i].bankName == bankName)
				accNoSelect.append('<option value="'+accounts[i].accNo+'">'+accounts[i].accNo+' - '+ accounts[i].accName+'</option>');
		}
	});
});
</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >支付查询</th>
  </tr>
  <tr>
      <td>  	
      <form action="list.htm" method="get">
	  <table style="margin:0 auto; ">
	  	<tr>
	  		<td style="width: 13%">交易日期：</td>
	  		<td><input id="transDate" name="transDate" class="textInput" value="${transDate}"/></td>
	  		<td style="width: 13%">银行：</td>
	  		<td>
				<select name="bankName">
					<option value="">---请选择银行---</option>
					<c:forEach items="${bankProfiles}" var="bankProfile">
						<option value="${bankProfile.bankName}" ${param.bankName == bankProfile.bankName ? 'selected="selected"' : ''}>${bankProfile.bankFullName}</option>
					</c:forEach>
				</select>
			</td>
			<td style="width: 13%">账号：</td>
			<td>
				<select name="accNo">
					<option value="">---------------------请选择账号-------------------</option>
				</select>
			</td>
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
                      <td width="10%">批次号</td>
                      <td width="8%">公司账号（出款账号）</td>
                      <td width="5%">银行名称</td>
                      <td width="6%">客户端系统</td>
                      <td width="5%">总金额</td>
                      <td width="5%">总笔数</td>
                      <td width="3%">优先级</td>
                      <td width="3%">待处理</td>
                      <td width="5%">正在提交</td>
                      <td width="3%">已提交</td>
                      <td width="4%" style="color: green">交易成功</td>
                      <td width="4%" style="color: red">交易失败</td>
                      <td width="5%">交易撤消</td>
                      <td width="5%">其他状态</td>
                      <td width="8%">业务时间</td>
                      <td width="10%">操作</td>
                    </thead>
                  <c:forEach items="${pageBean.result}" var="bp">                    
				    <tr bgcolor="#FFFFFF">
				      <td>${bp.batchSeqId}</td>
                      <td >${bp.accNo }</td>
					  <td>${bp.bankFullName }</td>
                      <td>${bp.requestSystem}</td>
                      <td>${bp.batchAmount }</td>
                      <td>${bp.batchCount }</td>
                      <td>${bp.priority }</td>
                      <td id="init_${bp.batchSeqId}">N/A</td>
                      <td id="processing_${bp.batchSeqId}">N/A</td>
                      <td id="submmited_${bp.batchSeqId}">N/A</td>
                      <td style="color: green" id="success_${bp.batchSeqId}">N/A</td>
                      <td style="color: red" id="fail_${bp.batchSeqId}">N/A</td>
                      <td id="cancle_${bp.batchSeqId}">N/A</td>
                      <td id="other_${bp.batchSeqId}">N/A</td>
                      <td><fmt:formatDate value="${bp.transDate }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td>
                      	<a href="#" onclick="refreshStatus('${bp.batchSeqId}'); return false;">刷新状态</a>
                      	<a href="detail.htm?batchSeqId=${bp.batchSeqId}&accNo=${bp.accNo}">查看详情</a>
                      	<script>batchIds.push('${bp.batchSeqId}');</script>
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
<script>
function refreshAllStatus(){
	$.get('stat-statuses.htm?batchSeqId=' + batchIds.join(',') + '&t=' + new Date().getTime(), function(result){
		for(var i=0; i<result.length; i++){
	  	  $('#init_' + result[i].batchSeqId).html(result[i].init);
	  	  $('#processing_' + result[i].batchSeqId).html(result[i].processing);
	  	  $('#submmited_' + result[i].batchSeqId).html(result[i].submmited);
	  	  $('#success_' + result[i].batchSeqId).html(result[i].success);
	  	  $('#fail_' + result[i].batchSeqId).html(result[i].fail);
	  	  $('#cancle_' + result[i].batchSeqId).html(result[i].cancle);
	  	  $('#other_' + result[i].batchSeqId).html(result[i].other);
  	   }
	});
}
setTimeout('refreshAllStatus()', 100);
</script>
</body>
</html>
