package com.iboxpay.settlement.gateway.common.web;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.iboxpay.settlement.gateway.common.config.SystemConfig;

public class UserLoginFilter implements Filter {

    private boolean isIgnore(String reqUri) {
        String ignoreURIs[] = SystemConfig.ignoreURIsProperties.getVals();
        for (String ignoreURI : ignoreURIs) {
            if (reqUri.indexOf(ignoreURI) != -1) return true;
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String reqUri = req.getRequestURI();
        if (reqUri != null) reqUri = reqUri.replaceAll("/+", "/");
        if (isIgnore(reqUri)) {
            chain.doFilter(request, response);
        }else if(reqUri.indexOf(".htm")==-1){//添加样式，js脚本，图片资源过滤
        	chain.doFilter(request, response);
        }else {
                String redirectURL = reqUri + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
                HttpSession session = req.getSession();
                if (session.getAttribute("login") == null) {
                    resp.sendRedirect(req.getContextPath() + "/manage/user/login.htm?redirectURL=" + URLEncoder.encode(redirectURL, "UTF-8"));
                } else {
                    chain.doFilter(request, response);
                }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
