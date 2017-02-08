<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>用户列表 - 银企直联中间件</title>
<link href="/css/css.css" rel="stylesheet" type="text/css" />
<script type='text/javascript' src='../../js/jquery-1.7.1.js'></script>
<link href="../../css/style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<script type="text/javascript">

</script>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
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
                      <td height="22" colspan="13" align="left" style="font-size:16px">用户列表 <a href="edit.htm">+添加用户</a></td>
                    </tr>
                    <tr bgcolor="#EEEEEE">
                      <td>用户名</td>
                      <td>真实名字</td>
                      <td>最后登陆时间</td>
                      <td>创建时间</td>
                      <td>修改时间</td>
                      <td>操作</td>
                    </tr>
                  <c:forEach items="${list}" var="user">                    
				    <tr bgcolor="#FFFFFF">
				      <td>${user.name }</td>
                      <td>${user.realName}</td>
                      <td><fmt:formatDate value="${user.lastLoginTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><fmt:formatDate value="${user.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><fmt:formatDate value="${user.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                      <td><a href="delete.htm?name=${user.name}" onclick="return confirm('确认删除？')">删除</a></td>
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
