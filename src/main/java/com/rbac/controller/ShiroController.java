package com.rbac.controller;

import jakarta.servlet.http.HttpSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("shiro")
public class ShiroController {
    private final static Logger LOG = LoggerFactory.getLogger(ShiroController.class);

    @GetMapping("userLogin")
    public String login(String name, String pwd,
                        @RequestParam(defaultValue = "false") boolean rememberMe, HttpSession session) {
        //1.获取subject
        Subject subject = SecurityUtils.getSubject();
        //2.封装请求数据到token
        AuthenticationToken token = new UsernamePasswordToken(name, pwd, rememberMe);
        //3.调用login登录认证
        try {
            subject.login(token);
        } catch (IncorrectCredentialsException e) {
            LOG.error("password error", e);
            return "password error";
        } catch (UnknownAccountException e) {
            LOG.error("unknown account error", e);
            return "unknown account error";
        }
        session.setAttribute("user", token.getPrincipal().toString());
        return "success";
    }
}
