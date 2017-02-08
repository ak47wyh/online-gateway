<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>日志列表 - 银企直联中间件</title>
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
<link href="/css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">

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
                      <td height="22" colspan="13" align="center" style="font-size:16px">日志列表</td>
                    </tr>
                    <tr bgcolor="#EEEEEE">
<!--                    <td width="4%" align="center" height="30">选择</td>-->
                      <td width="10%">日志名</td>
                      <td width="10%">文件大小</td>
                      <td width="10%">最后修改时间</td>
                      <td width="8%">操作</td>
                    </tr>
                  <c:forEach items="${logFileList}" var="logFile">                    
				    <tr bgcolor="#FFFFFF">
				      <td height="20">${logFile.name }</td>
                      <td width="10%">${logFile.length }</td>
                      <td><fmt:formatDate value="${logFile.lastModified}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><a href="download.htm?file=${logFile.name}">下载</a></td>
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
