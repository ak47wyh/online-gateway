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
html { overflow-x: auto; overflow-y: auto; border:0;} 
-->
</style>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<script type='text/javascript' src='../../js/common.js'></script>
</head>

<body>
<script type="text/javascript">
function FrontEnd(id, bankName, str){
	this.bankName = bankName;
	this.id = id;
	this.str = str;
}
var frontEndList = [];
<c:forEach items="${frontEndList}" var="fe">
  frontEndList.push(new FrontEnd('${fe.id}', '${fe.bankName}', '${fe.name}(${fe.ip.val}:${fe.port.val})'));                  
</c:forEach>

function onSelectCompany(){
	var bankName = $('#companyAccSelect').find("option:selected").attr('bankName');
	if(!bankName)
		return;
	
	var bankFrontEndList = [];
	for(var i=0; i<frontEndList.length; i++){
		if(frontEndList[i].bankName == bankName)
			bankFrontEndList.push(frontEndList[i]);
	}
	var frontEndSelect = $('#frontEndSelect');
	frontEndSelect.empty();
	for(var i=0; i<bankFrontEndList.length; i++){
		frontEndSelect.append('<option value="'+bankFrontEndList[i].id+'" >' + bankFrontEndList[i].str + '</option>');
	}
}
function check(){
	var accNo = $.trim($('#companyAccSelect').val());
	var frontEndId = $.trim($('#frontEndSelect').val());
	if(accNo == ''){
		alert('请选择公司账号');
		return false;
	}
	if(frontEndId == ''){
		alert('请选择前置机');
		return false;
	}
	return true;
}
</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
      <th class="tablestyle_title" >账号与前置绑定</th>
  </tr>
  <tr><td>
  	<form action="bind.htm" method="post">
	  <table width="800px" style="margin:0 auto; ">
	  	<tr><td colspan="2"><font color="green">提示：当不进行绑定时，银行的所有账号都可以对该银行配置的的所有前置机发送请求。</font></td></tr>
	  	<tr>
	  		<td>公司账号：</td>
	  		<td colspan="3"><select id="companyAccSelect" name="accNo" size="3" onchange="onSelectCompany();" style="width: 500px"></select></td>
	  	</tr>
	  	<tr>
	  		<td>前置机：</td>
	  		<td><select id="frontEndSelect" name="frontEndId" class="textInput" size="3" style="width: 500px"></select></td>
	  	</tr>
	  	<tr>
	  		<td></td><td><input type="submit" value="绑定公司账号到前置机" onclick="return check();"/></td>
	  	</tr>
	  	<tr></tr>
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
                <td height="40" class="font42">
                  <table width="100%" border="0" cellpadding="4" cellspacing="1" bgcolor="#464646" class="newfont03">
                    <thead style="background-color: #EEEEEE; font-weight: bold; text-align: center;">
                      <td>账号</td>
                      <td>户名</td>
                      <td>银行名称</td>
                      <td>银行全称</td>
                      <td>前置机ID</td>
                      <td>前置机名称</td>
                      <td>前置机IP</td>
                      <td>前置机端口</td>
                      <td>前置机配置状态</td>
                      <td>绑定时间</td>
                      <td>操作</td>
                    </thead>
                  <c:forEach items="${list}" var="binding">                    
				    <tr bgcolor="#FFFFFF">
				      <td height="20">${binding.pk.account.accNo}</td>
                      <td >${binding.pk.account.accName }</td>
					  <td>${binding.pk.account.bankName }</td>
                      <td>${binding.pk.account.bankFullName }</td>
					  <td>${binding.pk.frontEnd.id}</td>
                      <td>${binding.pk.frontEnd.name}</td>
                      <td>${frontEndMap[binding.pk.frontEnd.id].ip.val}</td>
                      <td>${frontEndMap[binding.pk.frontEnd.id].port.val}</td>
                      <td>${binding.pk.frontEnd.status == 'enable' ? '<font color="green">启用</font>' : '<font color="red">禁用</font>'}</td>
                      <td><fmt:formatDate value="${binding.updateTime }" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td>
                      	<a href="unbind.htm?accNo=${binding.pk.account.accNo}&frontEndId=${binding.pk.frontEnd.id}" onclick="javascript:return confirm('确定解除绑定吗？')">解除绑定</a>
                      	<c:if test="${not frontEndMap[binding.pk.frontEnd.id].enable}">
                      		&nbsp;<a href="../frontend/enable.htm?id=${binding.pk.frontEnd.id}" onclick="javascript:return confirm('确定启用该前置机吗？')">启用前置机</a>
                      	</c:if>
                      	<c:if test="${frontEndMap[binding.pk.frontEnd.id].enable}">
                      		&nbsp;<a href="../frontend/disable.htm?id=${binding.pk.frontEnd.id}" onclick="javascript:return confirm('确定禁用该前置机吗？')">禁用前置机</a>
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
