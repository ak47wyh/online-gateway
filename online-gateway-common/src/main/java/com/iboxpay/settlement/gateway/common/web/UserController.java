package com.iboxpay.settlement.gateway.common.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.UserEntity;
import com.iboxpay.settlement.gateway.common.util.MD5;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/user")
public class UserController {

    CommonDao userDao = CommonDaoImpl.getDao(UserEntity.class);

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView();
        List<UserEntity> list = userDao.findAll();
        mv.addObject("list", list);
        mv.setViewName("/views/user/list");
        return mv;
    }

    @RequestMapping(value = "login.htm", method = RequestMethod.GET)
    public ModelAndView login(@RequestParam(value = "redirectURL", required = false) String redirectURL) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView();
        if (redirectURL != null) mv.addObject("redirectURL", URLEncoder.encode(redirectURL, "UTF-8"));
        mv.setViewName("/views/user/login");
        return mv;
    }

    @RequestMapping(value = "login.htm", method = RequestMethod.POST)
    public ModelAndView doLogin(HttpServletRequest request, @RequestParam(value = "name") String name, @RequestParam(value = "password") String password,
            @RequestParam(value = "redirectURL", required = false) String redirectURL) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView();
        if (!StringUtils.isBlank(redirectURL)) mv.addObject("redirectURL", URLEncoder.encode(redirectURL, "UTF-8"));
        mv.setViewName("/views/user/login");
        String pwd = StringUtils.isBlank(password) ? null : MD5.encode(password);
        UserEntity userEntity = (UserEntity) userDao.get(name);
        if (userEntity == null) {
            mv.addObject("message", "用户名或者密码错误");
            return mv;
        } else {
            if (StringUtils.isBlank(userEntity.getPassword()) && StringUtils.isBlank(pwd)) {} else if (userEntity.getPassword() != null && userEntity.getPassword().equals(pwd)) {} else {
                mv.addObject("message", "用户名或者密码错误");
                return mv;
            }
        }
        userEntity.setLastLoginTime(new Date());
        userDao.update(userEntity);
        request.getSession().setAttribute("login", Boolean.TRUE);
        request.getSession().setAttribute("user", userEntity);
        if (StringUtils.isBlank(redirectURL)){
        	redirectURL = "/manage/index.htm";
        }
        
        redirectURL = redirectURL.replace(request.getContextPath(), "");
        
        return new ModelAndView("redirect:" + redirectURL);
    }

    @RequestMapping(value = "logout.htm", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        request.getSession().removeAttribute("login");
        request.getSession().removeAttribute("user");
        return new ModelAndView("redirect:login.htm");
    }

    @RequestMapping(value = "password.htm", method = RequestMethod.GET)
    public ModelAndView password() {
        return new ModelAndView("/views/user/password");
    }

    @RequestMapping(value = "password.htm", method = RequestMethod.POST)
    public ModelAndView changePassword(HttpServletRequest request, @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword, @RequestParam(value = "confPassword", required = false) String confPassword) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/user/password");
        UserEntity userEntity = (UserEntity) request.getSession().getAttribute("user");
        String pwd = StringUtils.isBlank(oldPassword) ? null : MD5.encode(oldPassword);
        if (StringUtils.isBlank(userEntity.getPassword()) && StringUtils.isBlank(pwd) || userEntity.getPassword() != null && userEntity.getPassword().equals(pwd)) {} else {
            mv.addObject("message", "“旧密码”错误");
            return mv;
        }
        if (StringUtils.isBlank(newPassword)) {
            mv.addObject("message", "请输入“新密码”");
            return mv;
        }
        if (StringUtils.isBlank(confPassword)) {
            mv.addObject("message", "请输入“确认密码”");
            return mv;
        }
        if (!newPassword.equals(confPassword)) {
            mv.addObject("message", "“新密码”与“确认密码”不一致");
            return mv;
        }
        userEntity.setPassword(MD5.encode(newPassword));
        userDao.update(userEntity);
        mv.addObject("message", "密码修改成功！");
        return mv;
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(@RequestParam(value = "user_name", required = false) String userName) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView();
        mv.addObject("user_name", userName);
        mv.setViewName("/views/user/edit");
        return mv;
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.POST)
    public ModelAndView submitEdit(@RequestParam(value = "user_name", required = false) String userName, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "password", required = false) String password) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("user_name", userName);
        mv.setViewName("/views/user/edit");
        Date now = new Date();
        UserEntity userEntity;
        if (StringUtils.isBlank(userName)) {//添加
            UserEntity _userEntity = (UserEntity) userDao.get(name);
            if (_userEntity != null) {
                mv.addObject("message", "用户名已存在");
                return mv;
            }
            if (name.getBytes().length > name.length()) {
                mv.addObject("message", "用户名请使用英语或数字");
                return mv;
            }
            userEntity = new UserEntity();
            userEntity.setName(name);
        } else {
            userEntity = (UserEntity) userDao.get(userName);
        }
        userEntity.setRealName(realName);
        if (!StringUtils.isBlank(password)) userEntity.setPassword(MD5.encode(password));
        userEntity.setType(0);
        userEntity.setCreateTime(now);
        userEntity.setUpdateTime(now);
        if (StringUtils.isBlank(userName))
            userDao.save(userEntity);
        else userDao.update(userEntity);

        return new ModelAndView("redirect:list.htm");
    }

    @RequestMapping(value = "delete.htm", method = RequestMethod.GET)
    public ModelAndView delete(@RequestParam(value = "name", required = false) String name) {
        userDao.delete(name);
        return new ModelAndView("redirect:list.htm");
    }
}
