package com.rbac.service;

import com.rbac.common.PageResult;
import com.rbac.pojo.FilterChainItem;

import java.util.List;

public interface FilterChainService {
    List<FilterChainItem> getFilterChainList();
    FilterChainItem getFilterChainById(long id);
    void addFilterChain(FilterChainItem filterChainItem);
    void updateFilterChain(FilterChainItem filterChainItem);
    void deleteFilterChainById(long id);

    PageResult<FilterChainItem> getPage(int pageIndex, int pageSize);
}
