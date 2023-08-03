package com.rbac.shiro.custom;

import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomFilterChainManager extends DefaultFilterChainManager {
    private String loginUrl;
    private String successUrl;
    private String unauthorizedUrl;

    @PostConstruct
    public void init(){
        Map<String, Filter> filters = getFilters();
        for (Map.Entry<String, Filter> filterEntry:filters.entrySet()){
            applyUnauthorizedUrlIfNecessary(filterEntry.getValue());
        }
    }
    /**
     * 构建默认过滤器链 加载默认过滤器
     */
    public CustomFilterChainManager() {
        setFilters(new LinkedHashMap<>());
        setFilterChains(new LinkedHashMap<>());
        addDefaultFilters(true);
    }

    /**
     * 构建自定义过滤器
     */
    public void setCustomFilters(Map<String, Filter> customFilters){
        for (Map.Entry<String, Filter> filterEntry:customFilters.entrySet()){
            addFilter(filterEntry.getKey(),filterEntry.getValue(),false);
        }
    }

    @Override
    protected void initFilter(Filter filter) {
        // super.initFilter(filter);
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getUnauthorizedUrl() {
        return unauthorizedUrl;
    }

    public void setUnauthorizedUrl(String unauthorizedUrl) {
        this.unauthorizedUrl = unauthorizedUrl;
    }
    private void applyLoginUrlIfNecessary(Filter filter) {
        String loginUrl = this.getLoginUrl();
        if (StringUtils.hasText(loginUrl) && filter instanceof AccessControlFilter) {
            AccessControlFilter acFilter = (AccessControlFilter)filter;
            String existingLoginUrl = acFilter.getLoginUrl();
            if ("/login.jsp".equals(existingLoginUrl)) {
                acFilter.setLoginUrl(loginUrl);
            }
        }

    }

    private void applySuccessUrlIfNecessary(Filter filter) {
        String successUrl = this.getSuccessUrl();
        if (StringUtils.hasText(successUrl) && filter instanceof AuthenticationFilter) {
            AuthenticationFilter authcFilter = (AuthenticationFilter)filter;
            String existingSuccessUrl = authcFilter.getSuccessUrl();
            if ("/".equals(existingSuccessUrl)) {
                authcFilter.setSuccessUrl(successUrl);
            }
        }

    }

    private void applyUnauthorizedUrlIfNecessary(Filter filter) {
        String unauthorizedUrl = this.getUnauthorizedUrl();
        if (StringUtils.hasText(unauthorizedUrl) && filter instanceof AuthorizationFilter) {
            AuthorizationFilter authzFilter = (AuthorizationFilter)filter;
            String existingUnauthorizedUrl = authzFilter.getUnauthorizedUrl();
            if (existingUnauthorizedUrl == null) {
                authzFilter.setUnauthorizedUrl(unauthorizedUrl);
            }
        }

    }
    private void applyGlobalPropertiesIfNecessary(Filter filter) {
        this.applyLoginUrlIfNecessary(filter);
        this.applySuccessUrlIfNecessary(filter);
        this.applyUnauthorizedUrlIfNecessary(filter);
    }


}
