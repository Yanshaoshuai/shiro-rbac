package com.rbac.config;

import com.alibaba.fastjson2.JSONObject;
import com.rbac.common.Result;
import com.rbac.utils.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.security.PublicKey;

public class JwtAuthenticationFilter extends AuthenticatingFilter {
    private final PublicKey publicKey;
    private final static Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        return super.preHandle(request, response);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String token = WebUtils.toHttp(request).getHeader("token");
        if (StringUtils.isEmpty(token)) {
            Unauthorized(response);
        }
        return new BearerToken(token, request.getRemoteHost());
    }

    /**
     * 是否允许访问
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        String token = WebUtils.toHttp(request).getHeader("token");
        if (StringUtils.isNotEmpty(token)) {
            try {
                JWTUtil.getClaims(token, publicKey);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 访问拒绝时调用
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        Unauthorized(response);
        return false;
    }

    private static void Unauthorized(ServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            response.getWriter().write(JSONObject.toJSONString(Result.error(HttpStatus.UNAUTHORIZED)));
        } catch (IOException e) {
            LOG.error("response error", e);
        }
    }
}
