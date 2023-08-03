package com.rbac.controller;

import com.rbac.common.Result;
import com.rbac.utils.JWTUtil;
import io.jsonwebtoken.SignatureAlgorithm;
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
import org.springframework.web.bind.annotation.RestController;

import java.security.PrivateKey;


@RestController
@RequestMapping("shiro")
public class ShiroController {
    private final static Logger LOG = LoggerFactory.getLogger(ShiroController.class);
    private final PrivateKey privateKey;
    public ShiroController(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @GetMapping("getToken")
    public Result<String> getToken(String name, String pwd) {
        //1.获取subject
        Subject subject = SecurityUtils.getSubject();
        //2.封装请求数据到token
        AuthenticationToken token = new UsernamePasswordToken(name, pwd);
        //3.调用login登录认证
        try {
            subject.login(token);
        } catch (IncorrectCredentialsException e) {
            LOG.error("password error", e);
            return Result.error("password error");
        } catch (UnknownAccountException e) {
            LOG.error("unknown account error", e);
            return Result.error("unknown account error");
        }
        return Result.ok(JWTUtil.getToken(name,subject.getSession().getTimeout()*1000, SignatureAlgorithm.RS512,privateKey,subject.getSession().getId().toString()));
    }

    @GetMapping("access")
    public Result<String> getAccessInfo(){
        return null;
    }

    @GetMapping("hasAccess")
    public Result<Boolean> hasAccess(String url){
        Subject subject = SecurityUtils.getSubject();
        return Result.ok( subject.isPermitted(url));
    }
    @GetMapping("hello")
    public String hello(){
        return "hello";
    }
}
