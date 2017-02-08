<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%
	String queryString = request.getQueryString();
	if(queryString != null){
		queryString = queryString.replaceAll("(.*)&?page=\\d+", "$1");
		System.out.println(queryString);
		request.setAttribute("queryString", queryString.length() > 0 ? queryString + (queryString.endsWith("&") ? "" : "&") : "");
	}
%>
<span>
<c:if test="${pageBean.pageNo > 1}"><a href="?${queryString}page=${pageBean.pageNo-1}" style="color: white; font-size: 12ox">上一页</a></c:if>
<c:if test="${pageBean.pageNo == 1}">上一页</c:if>
&nbsp;&nbsp;
<c:if test="${pageBean.pageNo < pageBean.totalPages}"><a href="?${queryString}page=${pageBean.pageNo+1}" style="color: white; font-size: 12ox">下一页</a></c:if>
<c:if test="${pageBean.pageNo == pageBean.totalPages}">下一页</c:if>
<span style="color: white;">(当前页=${pageBean.pageNo}, 总页数=${pageBean.totalPages}, 总数=${pageBean.totalCount})</span>
</span>
