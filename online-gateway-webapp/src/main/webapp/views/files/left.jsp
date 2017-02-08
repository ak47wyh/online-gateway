<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>管理首页 - 银企直联中间件</title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
	background-image: url(../../images/left.gif);
}
-->
</style>
<link href="../../css/css.css" rel="stylesheet" type="text/css" />
</head>
<SCRIPT language=JavaScript>
function tupian(idt){
    var nametu="xiaotu"+idt;
    var tp = document.getElementById(nametu);
    tp.src="../../images/ico05.gif";
	
	for(var i=1;i<30;i++)
	{
	  
	  var nametu2="xiaotu"+i;
	  if(i!=idt*1)
	  {
	    var tp2=document.getElementById('xiaotu'+i);
		if(tp2!=undefined)
	    {tp2.src="../../images/ico06.gif";}
	  }
	}
}

function list(idstr){
	var name1="subtree"+idstr;
	var name2="img"+idstr;
	var objectobj=document.all(name1);
	var imgobj=document.all(name2);
	
	
	//alert(imgobj);
	
	if(objectobj.style.display=="none"){
		for(i=1;i<10;i++){
			var name3="img"+i;
			var name="subtree"+i;
			var o=document.all(name);
			if(o!=undefined){
				o.style.display="none";
				var image=document.all(name3);
				//alert(image);
				image.src="../../images/ico04.gif";
			}
		}
		objectobj.style.display="";
		imgobj.src="../../images/ico03.gif";
	}
	else{
		objectobj.style.display="none";
		imgobj.src="../../images/ico04.gif";
	}
}

</SCRIPT>

<body>
<table width="198" border="0" cellpadding="0" cellspacing="0" class="left-table01">
  <tr>
    <TD>
		<table width="100%" border="0" cellpadding="0" cellspacing="0">
		  <tr>
			<td width="207" height="55" background="../../images/nav01.gif">
				<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
				  <tr>
					<td width="25%" rowspan="2"><img src="../../images/ico02.gif" width="35" height="35" /></td>
					<td width="75%" height="22" class="left-font01">您好<span class="left-font02">king</span></td>
				  </tr>
				  <tr>
					<td height="22" class="left-font01">
						[&nbsp;<a href="../../login.html" target="_top" class="left-font01">退出</a>&nbsp;]</td>
				  </tr>
				</table>
			</td>
		  </tr>
		</table>

		<!--  管理模块开始   -->
		<TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="left-table03">
          <tr>
            <td height="29">
				<table width="85%" border="0" align="center" cellpadding="0" cellspacing="0">
					<tr>
						<td width="8%"><img name="img8" id="img8" src="../../images/ico04.gif" width="8" height="11" /></td>
						<td width="92%">
								<a href="javascript:" target="mainFrame" class="left-font03" onClick="list('8');" >管理</a></td>
					</tr>
				</table>
			</td>
          </tr>		  
        </TABLE>
		<table id="subtree8" style="DISPLAY: none" width="80%" border="0" align="center" cellpadding="0" 
				cellspacing="0" class="left-table02">
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu20" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/frontend/list.htm" target="mainFrame" class="left-font03" onClick="tupian('20');">前置机管理</a></td>
				</tr>
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu21" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/account/list.htm" target="mainFrame" class="left-font03" onClick="tupian('21');">公司账号管理</a></td>
				</tr>
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu21" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/merchant/list.htm" target="mainFrame" class="left-font03" onClick="tupian('26');">交易账号管理</a></td>
				</tr>
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu22" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/accfrontend/list.htm" target="mainFrame" class="left-font03" onClick="tupian('22');">账号前置绑定</a></td>
				</tr>
				<c:if test="${sessionScope.user.admin}">
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu990" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/user/list.htm" target="mainFrame" class="left-font03" onClick="tupian('990');">用户管理</a></td>
				</tr>
				</c:if>
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu23" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/config/list.htm" target="mainFrame" class="left-font03" onClick="tupian('23');">参数管理</a></td>
				</tr>		
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu24" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/cache/index.htm" target="mainFrame" class="left-font03" onClick="tupian('24');">缓存管理</a></td>
				</tr>				
				<tr>
				  <td width="9%" height="21" ><img id="xiaotu25" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/schedule/list.htm" target="mainFrame" class="left-font03" onClick="tupian('25');">定时任务配置</a></td>
				</tr>				
						
      </table>
		<!--  管理模块结束    -->
		<!--  系统模块开始    -->
		<TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="left-table03">
          <tr>
            <td height="29">
				<table width="85%" border="0" align="center" cellpadding="0" cellspacing="0">
					<tr>
						<td width="8%"><img name="img7" id="img7" src="../../images/ico04.gif" width="8" height="11" /></td>
						<td width="92%">
								<a href="javascript:" target="mainFrame" class="left-font03" onClick="list('7');" >系统</a></td>
					</tr>
				</table>
			</td>
          </tr>		  
        </TABLE>
		<table id="subtree7" style="DISPLAY: none" width="80%" border="0" align="center" cellpadding="0" 
				cellspacing="0" class="left-table02">
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu17" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%">
						<a href="../../manage/system/stat.htm" target="mainFrame" class="left-font03" onClick="tupian('17');">运行统计</a></td>
				</tr>
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu18" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%">
					<a href="../../manage/system/init.htm" target="mainFrame" class="left-font03" onClick="tupian('18');">系统初始化</a></td>
				</tr>
      </table>
		<!--  系统模块结束    -->
		<%--业务测试模块开始：--%>
        <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="left-table03">
          <tr>
            <td height="29">
				<table width="85%" border="0" align="center" cellpadding="0" cellspacing="0">
					<tr>
						<td width="8%"><img name="img1" id="img1" src="../../images/ico04.gif" width="8" height="11" /></td>
						<td width="92%">
							<a href="javascript:" target="mainFrame" class="left-font03" onClick="list('1');" >业务测试</a></td>
					</tr>
				</table>
			</td>
          </tr>		  
        </TABLE>
		<table id="subtree1" style="DISPLAY: none" width="80%" border="0" align="center" cellpadding="0" 
				cellspacing="0" class="left-table02">
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu1" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/account/list.htm" target="mainFrame" class="left-font03" onClick="tupian('1');">余额</a></td>
				</tr>
				<%-- 
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu4" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/payment/list.htm" target="mainFrame" class="left-font03" onClick="tupian('4');">支付</a></td>
				</tr>
				--%>
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu10" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/query/list.htm" target="mainFrame" class="left-font03" onClick="tupian('10');">支付查询</a></td>
				</tr>
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu11" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/detail/list.htm" target="mainFrame" class="left-font03" onClick="tupian('11');">交易明细</a></td>
				</tr>
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu12" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/detail/query-records.htm" target="mainFrame" class="left-font03" onClick="tupian('12');">交易明细查询记录</a></td>
				</tr>
				<tr>
				  <td width="9%" height="20" ><img id="xiaotu13" src="../../images/ico06.gif" width="8" height="12" /></td>
				  <td width="91%"><a href="../../manage/check/list.htm" target="mainFrame" class="left-font03" onClick="tupian('13');">交易对账</a></td>
				</tr>
      </table>
		<%--业务测试模块结束：--%>

	  <!--  银行列表开始    -->
	  <table width="100%" border="0" cellpadding="0" cellspacing="0" class="left-table03">
          <tr>
            <td height="29"><table width="85%" border="0" align="center" cellpadding="0" cellspacing="0">
                <tr>
                  <td width="8%" height="12"><img name="img2" id="img2" src="../../images/ico04.gif" width="8" height="11" /></td>
                  <td width="92%"><a href="javascript:" target="mainFrame" class="left-font03" onClick="list('2');" >银行列表信息查询</a></td>
                </tr>
            </table></td>
          </tr>
      </table>
	  <table id="subtree2" style="DISPLAY: none" width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="left-table02">
		<tr>
          <td width="9%" height="20" ><img id="xiaotu7" src="../../images/ico06.gif" width="8" height="12" /></td>
          <td width="91%"><a href="../../manage/indexb.htm" target="mainFrame" class="left-font03" onClick="tupian('7');">当前银行列表</a></td>
        </tr>
      </table>
	  <!--  银行列表结束    -->
	  <!--  日志下载开始    -->
	  <table width="100%" border="0" cellpadding="0" cellspacing="0" class="left-table03">
          <tr>
            <td height="29"><table width="85%" border="0" align="center" cellpadding="0" cellspacing="0">
                <tr>
                  <td width="8%" height="12"><img name="img6" id="img6" src="../../images/ico04.gif" width="8" height="11" /></td>
                  <td width="92%"><a href="javascript:" target="mainFrame" class="left-font03" onClick="list('6');" >系统日志</a></td>
                </tr>
            </table></td>
          </tr>
      </table>
	  <table id="subtree6" style="DISPLAY: none" width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="left-table02">
		<tr>
          <td width="9%" height="20" ><img id="xiaotu7" src="../../images/ico06.gif" width="8" height="12" /></td>
          <td width="91%"><a href="../../manage/log/list.htm" target="mainFrame" class="left-font03" onClick="tupian('6');">日志下载</a></td>
        </tr>
      </table>
	  <!--  日志下载结束    -->	  
	  </TD>
  </tr>
  
</table>
</body>
</html>
