<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>交易明细 - 银企直联中间件</title>
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
function queryDetail(pageNo){
	var accNo = $("#companyAccSelect").val();
	if($.trim(accNo) == ''){
		alert('请选择公司账号');
		return false;
	}
	var beginDate = $("[name=beginDate]").val();
	var endDate = $("[name=endDate]").val();
	var forceUpdate = $("[name=forceUpdate]").is(':checked');
	var customerAccNo = $("[name=customerAccNo]").val();
	var customerAccName = $("[name=customerAccName]").val();
	var beginAmount = $("[name=beginAmount]").val();
	var endAmount = $("[name=endAmount]").val();
	var isQueryCredit = false;
	$("[name=isQueryCredit]").each(function(){
		if($(this).attr('checked')){
			isQueryCredit = 'true' == $(this).val();
		}
	});
	var req = {
		'appCode' : accNo,
		'forceUpdate' : forceUpdate,
		'beginDate' : beginDate,
		'endDate' : endDate,
		'customerAccNo' : customerAccNo,
		'customerAccName' : customerAccName,
		'beginAmount' : beginAmount,
		'endAmount' : endAmount,
		'queryCredit' : isQueryCredit,
		'pageSize' : 50,
		'pageNo' : pageNo
	};
	$('#loadFlag').show();
	var reqStr = JSON.stringify(req);
	//add it by caolipeng at 2015-06-02 start
	var str = "../../manage/detail/export.htm?accNo="+accNo+"&beginDate="+beginDate+"&endDate="+endDate;
	$('#exportId').attr('href',str);
	//add it by caolipeng at 2015-06-02 end
    $.ajax({
     	type: "POST",
     	url:"${pageContext.request.contextPath}/services/online/detail", 
     	dataType:"json",
     	data:reqStr,
     	contentType: "application/json",
     	success: function(result){ 
	     	$('#loadFlag').hide();
     		if(result.status == 'fail'){
     			alert(result.errorMsg);
     			return;
     		}
     		$('#detail_table').show();
			$('[name=detail_tr]').remove();
     		var pageBean = {};
     		pageBean.pageNo = result.pageNo;
			pageBean.totalPages = result.totalPages;
			pageBean.totalCount = result.totalCount;
			var detailsStr = '';
			for(var i=0; i<result.detailModelInfos.length; i++)
			{
			  $('#exportDetail').show();//显示下载交易明细链接 ，add it by caolipeng at 2015-06-02
	      	  detailsStr += '<tr name="detail_tr" bgcolor="#FFFFFF">'+
	      	  '<td>'+result.detailModelInfos[i].id+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].accNo)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].customerAccNo)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].customerAccName)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].customerBankFullName)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].debitAmount)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].creditAmount)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].balance)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].transDate)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].remark)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].bankBatchSeqId)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].useCode)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].useDesc)+'</td>'+
              '<td>'+$.trim(result.detailModelInfos[i].updateTime)+'</td>'+
              '</tr>';
			}
			$('#details').after(detailsStr);
			pager('pager_td', pageBean, 'queryDetail');
     	},
     	error : function (XMLHttpRequest, textStatus, errorThrown) {
	     	$('#loadFlag').hide();
     		alert(textStatus + ':' + errorThrown);
		}
     });
}
function exportExcel(){
	
}
</script>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
      <th class="tablestyle_title" >交易	明细</th>
  </tr>
  <tr><td>
  	<form action="list.htm">
	  <table width="800px" style="margin:0 auto; ">
	  	<tr><td>公司账号：</td><td colspan="3"><select id="companyAccSelect"></select></td></tr>
	  	<tr>
	  		<td style="width: 13%">交易日期：</td>
	  		<td><input name="beginDate" id="beginDate" class="textInput" value="${today}"/></td>
	  		<td style="width: 13%">——</td>
	  		<td><input name="endDate" id="endDate" class="textInput" value="${today}"/></td>
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
	  		<td><input type="radio" name="isQueryCredit" value="false" id="credit" checked="checked"/><label for="credit">借方</label></td>
	  		<td><input type="radio" name="isQueryCredit" id="debit" value="true"/><label for="debit">贷方</label></td>
	  	</tr>
	  	<tr>
	  		<td></td>
	  		<td title="选择后每次都会发送请求到银行（特殊情况下使用！）"><input type="checkbox" id="forceUpdate" name="forceUpdate"/><label for="forceUpdate">强制获取最新数据</label></td>
	  	</tr>
	  	<tr><td></td><td colspan="2"><input type="submit" value="查询" style="width: 50px; margin-left: 200px;" onclick="queryDetail(); return false;"/>
	  	<span id="exportDetail" style="display: none;"><a id="exportId">导出明细</a></span>
	  	<span id="loadFlag" style="display: none;">加载中...</span></td><td></td></tr>
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
                      <td>对方账号</td>
                      <td>对方账户名</td>
                      <td>对方银行</td>
                      <td>借方金额</td>
                      <td>贷方金额</td>
                      <td>余额</td>
                      <td>交易时间</td>
                      <td>备注</td>
                      <td>银行批次流水</td>
                      <td>用途代码</td>
                      <td>用途描述</td>
                      <td>查询更新时间</td>
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
