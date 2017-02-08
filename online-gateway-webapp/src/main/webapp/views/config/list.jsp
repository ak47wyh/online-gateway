<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>参数管理 - 银企直联中间件</title>
<style>
.txt_input {
	width: 100%;
}
.txt_input_array {
	width: 75%;
}
</style>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<script>
function submit(bankName, propertyName, inputName){
	var values = [];
	$('[name=' + inputName + "]").each(function(){
		values.push($(this).val());
	});
  $.ajax(
  	{
	  	url : "${pageContext.request.contextPath}/manage/config/set.htm",
	  	type: "POST",
	  	data: {"bankName": bankName, "propertyName" : propertyName, "value": values},
	  	traditional : true,
	  	success : function(result){
	  		alert(result.statusMsg);
		}
  	}
  );
}
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
<table style="width: 100%">
<thead>
<tr><th>配置项</th><th width="20%">配置值</th><th>配置说明</th><th>定义位置（类）</th><th>操作</th></tr>
</thead>
<c:forEach items="${banksConfProps}" var="item">

	<c:if test="${item.key == 'system'}">
		<tr><td style="border-bottom:1pt dotted black; text-align: center;" colspan="5"></td></tr>
		<tr><td style="border-bottom:1pt dotted black; text-align: center; color: green; font-weight: bold;" colspan="5">系统配置</td></tr>
	</c:if>
	<c:if test="${item.key != 'system'}">
		<tr><td style="border-bottom:1pt dotted black; text-align: center;" colspan="5"></td></tr>
		<tr><td style="border-bottom:1pt dotted black; text-align: center; color: green; font-weight: bold;" colspan="5">${bankProfileMap[item.key].bankFullName}</td></tr>
	</c:if>
		<c:forEach items="${item.value}" var="p">
		<tr><td>${p.name }</td>
		<td>
			<c:if test="${not p.array}">
			<input name="${item.key}_${p.name}" value="${p.val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input"/>
			</c:if>
			<c:if test="${p.array}">
				<c:forEach items="${p.vals}" var="val" varStatus="st">
				<c:if test="${st.index > 0}"><span name="s_${item.key}_${p.name}"></c:if>
				<input name="${item.key}_${p.name}" value="${val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input_array"/>
				<c:if test="${st.index == 0 && not p.readOnly}"><input type="button" id="add_${item.key}_${p.name}" onclick="addExtInput('${item.key}_${p.name}', 'add_${item.key}_${p.name}', ${p.readOnly})" value="添加"/></c:if>
				<c:if test="${st.index > 0}"><input value="删除" type="button" id="del_${item.key}_${p.name}_${st.count}" onclick="delExtInput('del_${item.key}_${p.name}_${st.count}')"/></c:if>
				<c:if test="${st.index > 0}"></span></c:if>
				</c:forEach>
				<c:if test="${fn:length(p.vals) == 0}">
				<input name="${item.key}_${p.name}" value="${val}" ${p.readOnly ? "readonly='readonly'" : ""} class="txt_input_array"/>
				<c:if test="${not p.readOnly}">
					<input type="button" id="add_${item.key}_${p.name}" onclick="addExtInput('${item.key}_${p.name}', 'add_${item.key}_${p.name}', ${p.readOnly})" value="添加"/>
				</c:if>
				</c:if>		
			</c:if>
		</td>
		<td>${p.desc }</td><td>${p.sourceClass }</td>
		<td><input type="button" value="提交" onclick="submit('${item.key}', '${p.name}', '${item.key}_${p.name}')"/></td></tr>
		</c:forEach>
</c:forEach>
</table>
</body>
</html>
