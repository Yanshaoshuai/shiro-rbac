package com.rbac.config;

import com.alibaba.fastjson2.JSONObject;
import com.rbac.common.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class JwtPermissionFilter extends PermissionsAuthorizationFilter {
    /**
     * 访问拒绝时调用
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        String token = WebUtils.toHttp(request).getHeader("token");
        if (StringUtils.isNotEmpty(token)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(JSONObject.toJSONString(Result.error(HttpStatus.FORBIDDEN)));
        }else {
            return false;
        }

        return super.onAccessDenied(request, response);
    }
}
