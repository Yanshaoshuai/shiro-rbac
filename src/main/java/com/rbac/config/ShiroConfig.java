package com.rbac.config;

import com.rbac.realm.UsernamePasswordRealm;
import com.rbac.shiro.custom.CustomFilterChainManager;
import com.rbac.shiro.custom.CustomPathMatchingFilterChainResolver;
import com.rbac.shiro.custom.CustomShiroFilterFactoryBean;
import com.rbac.shiro.custom.JwtAuthenticationFilter;
import com.rbac.shiro.custom.JwtPermissionFilter;
import com.rbac.shiro.custom.JwtRolesFilter;
import com.rbac.shiro.custom.RedisSessionDao;
import com.rbac.shiro.custom.ShiroSessionManager;
import com.rbac.utils.Asymmetric;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import javax.servlet.Filter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    private final UsernamePasswordRealm usernamePasswordRealm;

    private final RedissonClient redissonClient;


    public ShiroConfig(UsernamePasswordRealm usernamePasswordRealm, RedissonClient redissonClient) {
        this.usernamePasswordRealm = usernamePasswordRealm;
        this.redissonClient = redissonClient;
    }

    @Bean
    public PrivateKey privateKey() {
        PrivateKey privateKey;
        try {
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "keys/privateKey");
            String privateKeyStr = FileCopyUtils.copyToString(new FileReader(file));
            byte[] decode = Base64.getMimeDecoder().decode(privateKeyStr);
            privateKey = Asymmetric.loadPrivateKey(decode, SignatureAlgorithm.RS512.getFamilyName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return privateKey;
    }

    @Bean
    public PublicKey publicKey() {
        PublicKey publicKey;
        try {
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "keys/publicKey");
            String publicKeyStr = FileCopyUtils.copyToString(new FileReader(file));
            byte[] decode = Base64.getMimeDecoder().decode(publicKeyStr);
            publicKey = Asymmetric.loadPublicKey(decode, SignatureAlgorithm.RS512.getFamilyName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return publicKey;
    }

    @Bean
    public RedisSessionDao redisSessionDao() {
        return new RedisSessionDao(redissonClient, 10*60*1000L);
    }

    public Map<String, Filter> filters(PublicKey publicKey) {
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt-authc", new JwtAuthenticationFilter(publicKey));
        filterMap.put("jwt-perms", new JwtPermissionFilter());
        filterMap.put("jwt-roles", new JwtRolesFilter());
        return filterMap;
    }

    /**
     * 创建会话管理器
     */
    @Bean
    public ShiroSessionManager defaultSessionManager(RedisSessionDao redisSessionDao, PublicKey publicKey) {
        ShiroSessionManager sessionManager = new ShiroSessionManager(publicKey);
        sessionManager.setSessionDAO(redisSessionDao);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setGlobalSessionTimeout(redisSessionDao.getExpire());
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setSessionIdCookie(simpleCookie());
        return sessionManager;
    }

    /**
     * 创建defaultWebSecurityManager
     */
    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(ShiroSessionManager shiroSessionManager) {
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
        defaultWebSecurityManager.setSessionManager(shiroSessionManager);
        return defaultWebSecurityManager;
    }


    @Bean(name = "sessionIdCookie")
    public SimpleCookie simpleCookie() {
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setName("ShiroSession");
        return simpleCookie;
    }


    /**
     * 设置shiro过滤器拦截范围
     */
    private Map<String, String> filterChainDefinitionMap() {
        Map<String, String> definition = new HashMap<>();
        // anon指定url可以匿名访问
        definition.put("/shiro/getToken", "anon");
        // 配置登出过滤器
        definition.put("/logout", "logout");
        // 放行swagger
        definition.put("/swagger-ui/**", "anon");
        definition.put("/swagger/**", "anon");
        definition.put("/webjars/**", "anon");
        definition.put("/swagger-resources/**", "anon");
        definition.put("/v2/**", "anon");
        definition.put("/v3/**", "anon");
        definition.put("/static/**", "anon");
        // 如果没有登录会跳到相应的登录页面登录
        // definition.put("/**", "authc");
        definition.put("/**", "jwt-authc");
        return definition;
    }

    @Bean
    public CustomFilterChainManager customFilterChainManager(PublicKey publicKey){
        CustomFilterChainManager customFilterChainManager=new CustomFilterChainManager();
        customFilterChainManager.setLoginUrl("/shiro/login");
        customFilterChainManager.setUnauthorizedUrl("/shiro/login");
        customFilterChainManager.setSuccessUrl("/home");
        customFilterChainManager.setCustomFilters(filters(publicKey));
        return customFilterChainManager;
    }

    @Bean
    public CustomPathMatchingFilterChainResolver customPathMatchingFilterChainResolver(CustomFilterChainManager filterChainManager){
        CustomPathMatchingFilterChainResolver filterChainResolver=new CustomPathMatchingFilterChainResolver();
        filterChainResolver.setFilterChainManager(filterChainManager);
        return filterChainResolver;
    }
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, CustomPathMatchingFilterChainResolver filterChainResolver) {
        CustomShiroFilterFactoryBean factoryBean = new CustomShiroFilterFactoryBean();
        factoryBean.setChainResolver(filterChainResolver);
        factoryBean.setSecurityManager(securityManager);
        return factoryBean;
    }
}
