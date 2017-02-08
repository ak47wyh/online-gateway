<%@ page language="java" contentType="text/html; charset=UTF-8"
	isErrorPage="true" pageEncoding="UTF-8"%>
<%
String className = request.getParameter("class");
if(className == null){
    out.print("请输入class参数");
    return;
}
out.print(getClass().getClassLoader().getResource(className.replace('.', '/') + ".class"));
%>
