package com.javademo.shiro.springboot.config;

import com.rbac.config.RedisSessionDao;
import com.rbac.realm.UsernamePasswordRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShiroConfig {
    private final UsernamePasswordRealm usernamePasswordRealm;

    private final RedissonClient redissonClient;

    public ShiroConfig(UsernamePasswordRealm usernamePasswordRealm, RedissonClient redissonClient) {
        this.usernamePasswordRealm = usernamePasswordRealm;
        this.redissonClient = redissonClient;
    }

    @Bean
    public RedisSessionDao redisSessionDao(){
        return new RedisSessionDao(redissonClient, 600L);
    }

    /**
     * 创建会话管理器
     */
    public DefaultWebSessionManager defaultSessionManager(RedisSessionDao redisSessionDao){
        DefaultWebSessionManager sessionManager=new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDao);
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setGlobalSessionTimeout(redisSessionDao.getExpire());
        return sessionManager;
    }
    /**
     * 创建defaultWebSecurityManager
     */
    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager() {
        // 1.创建defaultWebSecurityManager
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        // 2.创建加密对象 设置相关属性
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        // 设置算法
        credentialsMatcher.setHashAlgorithmName("MD5");
        // 设置迭代加密次数
        credentialsMatcher.setHashIterations(3);
        // 3.将加密对象存储到realm中
        usernamePasswordRealm.setCredentialsMatcher(credentialsMatcher);
        // 4.将realm设置到创建defaultWebSecurityManager中
        defaultWebSecurityManager.setRealm(usernamePasswordRealm);
        defaultWebSecurityManager.setRememberMeManager(rememberMeManager());
        return defaultWebSecurityManager;
    }



    private SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        // 设置跨域
        // cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(30 * 24 * 60 * 60);
        return cookie;
    }

    private RememberMeManager rememberMeManager() {
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(rememberMeCookie());
        rememberMeManager.setCipherKey("1234567890987654".getBytes());
        return rememberMeManager;
    }

    /**
     * 设置shiro过滤器拦截范围
     */
    @Bean
    public DefaultShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition definition = new DefaultShiroFilterChainDefinition();
        // anon指定url可以匿名访问
        definition.addPathDefinition("/shiro/userLogin", "anon");
        definition.addPathDefinition("/shiro/login", "anon");
        // 配置登出过滤器
        definition.addPathDefinition("/logout", "logout");
        // 如果没有登录会跳到相应的登录页面登录
        definition.addPathDefinition("/**", "authc");
        // 用户拦截器，用户已经身份验证/记住我登录的都可
        definition.addPathDefinition("/**", "user");
        return definition;
    }

    /**
     * 开启注解
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
}
