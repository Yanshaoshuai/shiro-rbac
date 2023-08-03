package com.rbac.pojo;

import java.io.Serializable;

public class FilterChainItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String urlPatternName;

    private String urlPattern;

    private String filterName;

    /**
     * 多个 逗号分割
     */
    private String roles;

    /**
     * 多个 逗号分割
     */
    private String permissions;

    private Integer order;

    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrlPatternName() {
        return urlPatternName;
    }

    public void setUrlPatternName(String urlPatternName) {
        this.urlPatternName = urlPatternName;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
