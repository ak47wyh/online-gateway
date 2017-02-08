<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>公司账号编辑 - 银企直联中间件</title>
<style>
.txt_input {
	width: 100%;
}
.txt_input_array {
	width: 80%;
}
</style>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<script>
function addExtInput(name, id, readOnly){
	var spanName = 's_'+name;
	var span = $('[name='+spanName+']');
	var length = span.size();
	var spanStr = '<span name="'+spanName+'"><input name="'+name+'" '+(readOnly ? 'readonly="readonly"' : '')+' class="txt_input_array"/>'
	+'&nbsp;<input value="删除" type="button" id="del_'+name+'_'+(length+1)+'" onclick="delExtInput(\'del_'+name+'_'+(length+1)+'\')"/>'
	+'</span>';
	if(length == 0)
		$('#'+id).after(spanStr);
	else
		span.eq(length-1).after(spanStr);
}
function delExtInput(id){
	$('#'+id).parent().remove();
}
</script>
</head>
<body>
<a href="list.htm">返回公司账号列表</a><br/>
${errorMsg }
<c:if test="${empty errorMsg}">
【${account.bankFullName}】公司账号配置：
<form action="edit.htm" method="post">
<input type="hidden" name="from_url_accNo" value="${from_url_accNo}"/>
<table style="width: 1000px">
<thead>
<tr><th>账户属性</th><th width="50%">配置值</th></tr>
</thead>
<tr><td style="border-bottom:1pt solid black;" colspan="2"></td></tr>
<tr><td>银行简码：</td><td><input name="bankName" value="${account.bankName }" readonly="readonly" class="txt_input"/></td></tr>
<tr><td>银行全称：</td><td><input name="bankFullName" value="${account.bankFullName }" readonly="readonly" class="txt_input"/></td></tr>
<tr><td>账号：</td><td><input name="accNo" value="${account.accNo }" class="txt_input" ${not empty account.accNo ? 'readonly="readonly"' : ''}/></td></tr>
<tr><td>户名：</td><td><input name="accName" value="${account.accName }" class="txt_input"/></td></tr>
<tr><td>支行名称：</td><td><input name="bankBranchName" value="${account.bankBranchName }" class="txt_input"/></td></tr>
<tr><td>开户地区号：</td><td><input name="areaCode" value="${account.realAreaCode }" class="txt_input"/></td></tr>
<tr><td>联行号：</td><td><input name="cnaps" value="${account.cnaps }" class="txt_input"/></td></tr>
<tr><td>设置为该银行默认账号（如果设置了当前为默认，则该银行其他账号自动变为非默认）：</td><td><input id="bankDefault" type="checkbox" name="bankDefault" value="true" ${account.bankDefault?"checked='checked'":"" }><label for="bankDefault">设为默认</label> </td></tr>
<tr><td>币别：</td><td><select name="currency"><option value="CNY">人民币</option></select></td></tr>
<c:if test="${fn:length(account.extPropertys) > 0}">
<tr><td colspan="2" style="border-bottom:1pt solid black; font-weight: bold;">账户扩展属性：</td></tr>
<c:forEach items="${account.extPropertys}" var="p">
	<tr>
	<td>${p.desc}：</td>
	<td>
		<c:if test="${not p.array}">
		<input name="ext_${p.name}" value="${p.val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input"/>
		</c:if>
		<c:if test="${p.array}">
			<c:forEach items="${p.vals}" var="val" varStatus="st">
			<c:if test="${st.index > 0}"><span name="s_ext_${p.name}"></c:if>
			<input name="ext_${p.name}" value="${val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input_array"/>
			<c:if test="${st.index == 0 && not p.readOnly}"><input type="button" id="add_ext_${p.name}" onclick="addExtInput('ext_${p.name}', 'add_ext_${p.name}', ${p.readOnly})" value="添加"/></c:if>
			<c:if test="${st.index > 0}"><input value="删除" type="button" id="del_ext_${p.name}_${st.count}" onclick="delExtInput('del_ext_${p.name}_${st.count}')"/></c:if>
			<c:if test="${st.index > 0}"></span></c:if>
			</c:forEach>
			<c:if test="${fn:length(p.vals) == 0}">
			<input name="ext_${p.name}" value="${val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input_array"/>
			<c:if test="${not p.readOnly}">
				<input type="button" id="add_ext_${p.name}" onclick="addExtInput('ext_${p.name}', 'add_ext_${p.name}', ${p.readOnly})" value="添加"/>
			</c:if>
			</c:if>			
		</c:if>
	</td>
	</tr>
	<tr><td style="border-bottom:1pt dotted black;" colspan="2"></td></tr>
</c:forEach>
</c:if>
<tr>
    <td>接口定制启用（<b>技术配置</b>）：</td>
    <td>
        <input type="radio" name="transConfigEnabled" value="false" ${account.transConfigEnabled ? '' : 'checked="checked"'} id="transConfigEnabled_false"/><label for="transConfigEnabled_false">禁用(使用<b>默认接口设置</b>)</label>
        <input type="radio" name="transConfigEnabled" value="true" ${account.transConfigEnabled ? 'checked="checked"' : ''} id="transConfigEnabled_true"/><label for="transConfigEnabled_true">启用</label>
        <a href="#" id="transDefaultLink" onclick="open('trans-config.htm?type=default&accNo=${account.accNo}', null, 'location=no, menubar=no, height=350'); return false;" ${account.transConfigEnabled ? 'style="display: none;"' : 'style="display: inline;"'}>查看<b>默认接口设置</b></a>
        <a href="#" id="transConfigLink" onclick="open('trans-config.htm?accNo=${account.accNo}', null, 'location=no, menubar=no, height=350'); return false;" ${account.transConfigEnabled ? 'style="display: inline;"' : 'style="display: none;"'}>接口定制设置</a>
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
</c:if>
<script>
$(document).ready(function() {
    $('input[type=radio][name=transConfigEnabled]').change(function() {
        if (this.value == 'true') {
        	$('#transConfigLink').show();
        	$('#transDefaultLink').hide();
        }else{
            $('#transConfigLink').hide();
            $('#transDefaultLink').show();
        }
    });
});
</script>
</body>
</html>
