<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号列表 - 银企直联中间件</title>
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
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">
function query_balance(accNo){
	$('#bal_q_' + accNo).html('查询中...');
  $.get("balance.htm",{"accNo": accNo}, function(result){
  	  eval('result=' + result);
  	  if(result.status == 'success'){
  	  	$('#bal_' + accNo).html('<font color="green">'+result.balance+'</font>');
  	  }else{
  	  	alert(result.errorMsg);
  	  }
  	  	$('#bal_q_' + accNo).html('查询');
  });
}	


function sousuo(){
	window.open("gaojisousuo.htm","","depended=0,alwaysRaised=1,width=800,height=510,location=0,menubar=0,resizable=0,scrollbars=0,status=0,toolbar=0");
}
function selectAll(){
	var obj = document.fom.elements;
	for (var i=0;i<obj.length;i++){
		if (obj[i].name == "delid"){
			obj[i].checked = true;
		}
	}
}

function unselectAll(){
	var obj = document.fom.elements;
	for (var i=0;i<obj.length;i++){
		if (obj[i].name == "delid"){
			if (obj[i].checked==true) obj[i].checked = false;
			else obj[i].checked = true;
		}
	}
}

function link(){
    document.getElementById("fom").action="addrenwu.htm";
   document.getElementById("fom").submit();
}

</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="30">
	  <table width="100%" border="0" cellspacing="0" cellpadding="0">	
        <tr>
          <td height="62" background="../../images/nav04.gif">
            <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
				<td>
				  <c:forEach items="${bankProfiles}" var="bankProfile">
					<a href="edit.htm?bankName=${bankProfile.bankName}">${bankProfile.bankFullName}</a>
				  </c:forEach>
				</td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <table id="subtree1" style="DISPLAY: " width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
            <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr>
                <td height="40" class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <tr class="CTitle" >
                      <td height="22" colspan="13" align="center" style="font-size:16px">账号列表</td>
                    </tr>
                    <tr bgcolor="#EEEEEE">
<!--                    <td width="4%" align="center" height="30">选择</td>-->
                      <td width="10%">账号</td>
                      <td width="8%">户名</td>
                      <td width="10%">银行名称</td>
                      <td width="8%">银行全称</td>
                      <td width="8%">支行名称</td>
                      <td width="8%">地区码</td>
                      <td width="8%">联行号</td>
                      <td width="4%">币别</td>
                      <td width="4%">接口定制启用</td>
                      <td width="8%">余额</td>
                      <td width="8%">创建时间</td>
                      <td width="8%">修改时间</td>
                      <td width="4%">操作</td>
                    </tr>
                  <c:forEach items="${list}" var="acc">                    
				    <tr bgcolor="#FFFFFF">
				      <td height="20">${acc.accNo }</td>
                      <td >${acc.accName }</td>
					  <td>${acc.bankName }</td>
                      <td>${acc.bankFullName }</td>
                      <td>${acc.bankBranchName }</td>
                      <td>${acc.areaCode }</td>
                      <td>${acc.cnaps }</td>
                      <td>${acc.currency }</td>
                      <td>${acc.transConfigEnabled ? '<font color="green">是</font>' : '否'}</td>
                      <td>【余额：<span id="bal_${acc.accNo}">N/A</span>】<a id="bal_q_${acc.accNo}" href="#" onclick="query_balance('${acc.accNo}')">查询</a></td>
                      <td><fmt:formatDate value="${acc.createTime }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><fmt:formatDate value="${acc.updateTime }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><a href="edit.htm?accNo=${acc.accNo}">编辑</a>&nbsp;<a href="delete.htm?accNo=${acc.accNo}" onclick="javascript:return confirm('确定删除该账号吗？')">删除</a></td>
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
