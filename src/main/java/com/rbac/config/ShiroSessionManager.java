package com.rbac.config;

import com.rbac.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.security.PublicKey;

public class ShiroSessionManager extends DefaultWebSessionManager {
    private final static Logger LOG = LoggerFactory.getLogger(ShiroSessionManager.class);
    private final PublicKey publicKey;

    public ShiroSessionManager(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String token = WebUtils.toHttp(request).getHeader("token");
        if (StringUtils.isEmpty(token)) {
            return super.getSessionId(request, response);
        }
        Claims body = JWTUtil.getBody(token, publicKey);
        String id = body.getId();
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, "STATELESS_REQUEST");
        if (id != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
        }
        return id;
    }
}
