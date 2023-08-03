package com.rbac.config;

import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CustomPathMatchingFilterChainResolver extends PathMatchingFilterChainResolver {
    private final static Logger LOG = LoggerFactory.getLogger(CustomPathMatchingFilterChainResolver.class);

    private  CustomFilterChainManager filterChainManager;

    public CustomFilterChainManager getFilterChainManager() {
        return this.filterChainManager;
    }

    public void setFilterChainManager(CustomFilterChainManager filterChainManager) {
        this.filterChainManager = filterChainManager;
    }

    @Override
    public FilterChain getChain(ServletRequest request, ServletResponse response, FilterChain originalChain) {
        FilterChainManager filterChainManager = this.getFilterChainManager();
        if (!filterChainManager.hasChains()) {
            return null;
        }
        String requestURI = getPathWithinApplication(request);
        for (String pathPattern:filterChainManager.getChainNames()){
            if(pathMatches(pathPattern,requestURI)){
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Matched path pattern [{}] for requestURI [{}].  Utilizing corresponding filter chain...", pathPattern, Encode.forHtml(requestURI));
                }

                return filterChainManager.proxy(originalChain, pathPattern);
            }
        }
        return null;
    }


}
