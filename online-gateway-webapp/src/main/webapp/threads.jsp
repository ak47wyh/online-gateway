<%@ page language="java" contentType="text/html; charset=UTF-8"
	isErrorPage="true" pageEncoding="UTF-8"%>
<%
	response.setStatus(HttpServletResponse.SC_OK);
	out.println("<!-- @Author:lidongbo -->");
	int depth = 100;
	java.util.Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
	for (java.util.Map.Entry<Thread, StackTraceElement[]> entry : map
			.entrySet()) {
		Thread t = entry.getKey();
		out.println("名称:&nbsp;&nbsp;&nbsp;" + t.getName() + "<BR>");
		out.println("状态:&nbsp;&nbsp;&nbsp;" + t.getState());
		out.println("<BR>");
		out.println("<BR>");
		StackTraceElement[] dump = entry.getValue();
		for (int j = 0, i = 0; i < dump.length && j < depth; i++, j++) {
			String el = dump[i].toString();
			out.println(el + "<BR>");
		}
		out.println("<BR>");
		out.println("<BR>");
		out
				.println("-------------------------------------------------------------------------");
		out.println("<BR>");
	}
%>
