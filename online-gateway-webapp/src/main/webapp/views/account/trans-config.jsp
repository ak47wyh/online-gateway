<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>交易接口配置列表</title>
<style type="text/css">
td{
    text-align: center;
}
button {
	padding: 3px;
}
</style>
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
</head>
<body>
<script type="text/javascript">
$(document).ready(function(){
	$('[name=button_up]').click(function(){
		var currentTr = $(this).parent().parent();
		var index = $('tbody tr').index(currentTr);
		if(index <= 0)
			return;
		var prev = currentTr.prev();
		if(prev.attr('name') == "trans_desc")
			return;
		currentTr.insertBefore(currentTr.prev());
	});
	$('[name=button_down]').click(function(){
		  var trs = $('tbody tr');
	      var currentTr = $(this).parent().parent();
	        var index = trs.index(currentTr);
	        if(index >= trs.size())
	            return;
	        var next = currentTr.next();
	        if(next.attr('name') == "trans_desc")
	            return;
	        currentTr.insertAfter(currentTr.next());
	});
	$('#button_save').click(function(){
		var bankTrans = [];
		$('tbody tr[id]').each(function(index, tr){
			var bankTran = {};
			bankTran.name = tr.id;
			bankTran.transComponentType = $(tr).attr('transComponentType');
			if('checked' == $(tr).find('[name="componentEnabled"]').attr('checked'))
				bankTran.componentEnabled = true;
			else
				bankTran.componentEnabled = false;
			bankTrans.push(bankTran);
		});
		//$.post('trans-config.htm', {'transConfigs' : ''});
		$.ajax({
			  type: 'POST',
			  url: 'trans-config.htm?accNo=${param.accNo}&transComponentType=${transComponentType}',
			  data: JSON.stringify(bankTrans),
			  success: function(data){
				    if(data.status == 'success')
				    	alert('保存成功');
				    else
				    	alert('保存异常：'+data.statusMsg);
			  },
			  contentType: "application/json", 
			  dataType: 'json'
		});
	});
});
</script>
<span style="color: red; font-weight: bold;">说明：按接口顺序，排在前面的优先级越高。绿色代表新添加的接口，中划线代表已删除接口。</span>
<table style="" width="100%" border="1" cellspacing="0" cellpadding="0">
<thead><tr><td>调整</td><td>接口</td><td>接口描述</td><td>启用</td><td>操作</td></tr></thead>
<tbody>
<c:if test="${not empty defTransMap}">
<c:forEach var="transEntry" items="${defTransMap}" varStatus="st">
 <tr name="trans_desc" style="color: red; font-weight: bold;"><td colspan="5">${transEntry.key == IPayment ? '支付接口' : transEntry.key == IBalance ? '余额' : transEntry.key == IDetail ? '交易明细' : '【未知接口】'}</td></tr>
 <c:forEach var="trans" items="${defTransMap[transEntry.key]}"> 
	 <tr id="${trans.class.name}">
	    <td><button name="button_up" disabled="disabled">↑</button><button name="button_down" disabled="disabled">↓</button></td>
	    <td><span style="font-weight: bold;">${trans.class.name}</span><div style="font-size: 8px; ">（${trans}）</div></td>
	    <td>${trans.bankTransDesc}</td>
	    <td><input type="checkbox" name="componentEnabled" checked="checked" disabled="disabled"/></td>
	    <td></td>
	 </tr>
 </c:forEach>
</c:forEach>
</c:if>
 
<c:if test="${not empty accountTransConfigsMap}">
<c:forEach var="transEntry" items="${accountTransConfigsMap}">
 <tr name="trans_desc" style="color: red; font-weight: bold;"><td colspan="5">${transEntry.key == IPayment ? '支付接口' : transEntry.key == IBalance ? '余额' : transEntry.key == IDetail ? '交易明细' : '【未知接口】'}</td></tr>
 <c:forEach var="transConfig" items="${accountTransConfigsMap[transEntry.key]}"> 
	 <tr id="${transConfig.bankTrans.class.name}" transComponentType="${transEntry.key.name}" style="${transConfig.componentNew ? 'background-color: #03FA4D;' : ''}${transConfig.componentExist ? '' : 'text-decoration: line-through;'}">
		 <td><button name="button_up">↑</button><button name="button_down">↓</button></td>
		 <td><span style="font-weight: bold;">${empty transConfig.bankTrans ? transConfig.pk.transComponent : transConfig.bankTrans.class.name}</span><div style="font-size: 8px; ">（${empty transConfig.bankTrans ? '【接口不存在】' : transConfig.bankTrans}）</div></td>
		 <td>${transConfig.bankTrans.bankTransDesc}</td>
		 <td><input type="checkbox" name="componentEnabled" ${transConfig.componentEnabled ? 'checked="checked"' : ''}/></td>
		 <td><c:if test="${empty transConfig.bankTrans}"><a href="delete-trans-config.htm?accNo=${param.accNo}&transComponent=${transConfig.pk.transComponent}" onclick="if(!confirm('确认删除不存在接口吗'))return false;">删除</a></c:if></td>
	 </tr> 
 </c:forEach>
</c:forEach>
</c:if>
</tbody>
<tfoot>
    <tr><td colspan="5"><button style="width: 70px;" id="button_save" ${empty defTrans ? '' : 'disabled="disabled"'}>保存设置</button></td></tr>
</tfoot>
</table>
</body>
</html>
