<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>对账 - 银企直联中间件</title>
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
	$("#transDate").datepicker();
	$('#ui-datepicker-div').hide();//jquery ui bug????
});
function getStatus(status, isPay){
	switch(status){
		case 0: return '待处理';
		case 10: return '正在提交';
		case 20: return '已提交';
		case 30: return '<font color="GREEN">交易成功</font>';
		case 35: return '交易撤消';
		case 40: return '<font color="RED">交易失败</font>';
		default: if(isPay)return '交易未确定'; else return '【无】';
	}
}
function checkPayment(pageNo){
	var accNo      = $("#companyAccSelect").val();
	if($.trim(accNo) == ''){
		alert('请选择公司账号');
		return false;
	}
	var transDate     = $("[name=transDate]").val();
	var customerAccNo     = $("[name=customerAccNo]").val();
	var customerAccName     = $("[name=customerAccName]").val();
	var beginAmount     = $("[name=beginAmount]").val();
	var endAmount     = $("[name=endAmount]").val();
	var status     = $("[name=status]").val();
	var hasCheck     = $("[name=hasCheck]").val();
	var req = {
		'appCode' : accNo,
		'transDate' : transDate,
		'customerAccNo' : customerAccNo,
		'customerAccName' : customerAccName,
		'beginAmount' : beginAmount,
		'endAmount' : endAmount,
		'status' : status,
		'hasCheck' : hasCheck,
		'pageSize' : 100,
		'pageNo' : pageNo
	};
	$('#loadFlag').show();
	var reqStr = JSON.stringify(req);
    $.ajax({
     	type: "POST",
     	url:"${pageContext.request.contextPath}/services/online/check", 
     	dataType:"json",
     	data:reqStr,
     	contentType: "application/json",
     	success: function(result){ 
	     	$('#loadFlag').hide();
     		if(result.status == 'fail'){
     			alert(result.errorMsg);
     			return;
     		}
     		$('#check_table').show();
			$('[name=check_tr]').remove();
     		var pageBean = {};
     		pageBean.pageNo = result.pageNo;
			pageBean.totalPages = result.totalPages;
			pageBean.totalCount = result.totalCount;
			var checksStr = '';
			if(result.checkModelInfos){
				for(var i=0; i<result.checkModelInfos.length; i++)
				{
		      	  checksStr += '<tr name="check_tr" bgcolor="#FFFFFF">'+
		      	  '<td>'+result.checkModelInfos[i].paymentId+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].accNo)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].customerAccNo)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].customerAccName)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].customerBankFullName)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].amount)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].transTime)+'</td>'+
	              '<td>'+getStatus(result.checkModelInfos[i].status, true)+'</td>'+
	              '<td>'+$.trim(result.checkModelInfos[i].statusMsg)+'</td>'+
	              '<td bgcolor="#EEEEEE">'+$.trim(result.checkModelInfos[i].detailId==0?'【无】':result.checkModelInfos[i].detailId)+'</td>'+
	              '<td bgcolor="#EEEEEE">'+(result.checkModelInfos[i].checkStatus == 30 ? '<font color="GREEN">交易成功</font>' : 
	              				result.checkModelInfos[i].checkStatus == 40 ? '<font color="RED">交易失败</font>' : '【无】')+'</td>'+
	              '<td bgcolor="#EEEEEE">'+$.trim(result.checkModelInfos[i].checkStatusMsg)+'</td>'+
	              '<td bgcolor="#EEEEEE">'+$.trim(result.checkModelInfos[i].updateTime)+'</td>'+
	              '</tr>';
				}
			}
			$('#checks').after(checksStr);
			pager('pager_td', pageBean, 'checkPayment');
			if($.trim(result.errorMsg) != '')
				alert(result.errorMsg);
     	},
     	error : function (XMLHttpRequest, textStatus, errorThrown) {
	     	$('#loadFlag').hide();
     		alert(textStatus + ':' + errorThrown);
		}
     });
}	
</script>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >对账</th>
  </tr>
  <tr><td>
  	<form action="list.htm">
	  <table width="800px" style="margin:0 auto; ">
	  	<tr><td>公司账号：</td><td colspan="3"><select id="companyAccSelect"></select></td></tr>
	  	<tr>
	  		<td style="width: 13%">交易日期：</td>
	  		<td><input name="transDate" id="transDate" class="textInput" value="${today}"/></td>
	  		<td style="width: 13%">交易状态</td>
	  		<td>
	  			<select name="status">
	  				<option value="-1">所有已提交银行的</option>
	  				<option value="30">交易成功</option>
	  				<option value="40">交易失败</option>
	  				<option value="50">交易未确定</option>
	  			</select>
	  		</td>
	  	</tr>
	  	<tr>
	  		<td>收款账号：</td>
	  		<td><input name="customerAccNo" class="textInput"/></td>
	  		<td>收款户名：</td>
	  		<td><input name="customerAccName" class="textInput"/></td>
	  	</tr>
	  	<tr>
	  		<td>交易金额：</td>
	  		<td><input name="beginAmount" style="width: 23%"/>-<input name="endAmount" style="width: 23%"/></td>
	  		<td>确认状态：</td>
	  		<td>
	  			<select name="hasCheck">
	  				<option value="">全部</option>
	  				<option value="checked">确认的</option>
	  				<option value="unchecked">未确认的</option>
	  			</select>
	  		</td>
	  		
	  	</tr>
	  	<tr>
	  		<td colspan="4"><font color="green">提示：对账后“交易明细ID”为空的和“确认状态”为空的，很可能已失败。</font></td>
	  	</tr>
	  	<tr><td></td><td colspan="2"><input type="submit" value="查询" style="width: 50px; margin-left: 200px;" onclick="checkPayment(); return false;"/><span id="loadFlag" style="display: none;">加载中...</span></td><td></td></tr>
	  </table>    
	  </form>
  </td></tr>
  <tr>
    <td>
      <table id="subtree1" width="95%" border="0" cellspacing="0" cellpadding="0" id="check_table">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <thead style="background-color: #EEEEEE; font-weight: bold; text-align: center;" id="checks">
	                  <td>支付ID</td>
                      <td>公司账号</td>
                      <td>对方账号</td>
                      <td>对方账户名</td>
                      <td>对方银行</td>
                      <td>交易金额</td>
                      <td>提交银行时间</td>
                      <td>支付状态</td>
                      <td>支付状态信息</td>
                      <td>对账的明细ID</td>
                      <td>对账状态</td>
                      <td>对账备注</td>
                      <td>对账时间</td>
                    </thead>
                  <tr>
                  	<td colspan="5" id="pager_td">
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
