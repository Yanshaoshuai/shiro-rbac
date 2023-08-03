package com.rbac.service.impl;

import com.rbac.common.PageResult;
import com.rbac.config.CustomFilterChainManager;
import com.rbac.mapper.FilterChainMapper;
import com.rbac.pojo.FilterChainItem;
import com.rbac.service.FilterChainService;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
public class FilterChainServiceImpl implements FilterChainService {
    private final FilterChainMapper filterChainMapper;
    private final CustomFilterChainManager filterChainManager;
    private final RedissonClient redissonClient;
    public FilterChainServiceImpl(FilterChainMapper filterChainMapper, CustomFilterChainManager filterChainManager, RedissonClient redissonClient) {
        this.filterChainMapper = filterChainMapper;
        this.filterChainManager = filterChainManager;
        this.redissonClient = redissonClient;
    }

    @PostConstruct
    public void init() {
        RList<FilterChainItem> filterChainItems = getFilterChainItems();
        initFilterChains(filterChainItems);
    }

    private RList<FilterChainItem> getFilterChainItems() {
        RList<FilterChainItem> filterChainItems = redissonClient.getList("shiro-filter-config");
        if(CollectionUtils.isEmpty(filterChainItems)){
            filterChainItems.addAll(getFilterChainList());
        }
        filterChainItems.expire(Duration.ofHours(1));
        return filterChainItems;
    }

    @Override
    public List<FilterChainItem> getFilterChainList() {
        return filterChainMapper.getList();
    }

    @Override
    public FilterChainItem getFilterChainById(long id) {
        return filterChainMapper.selectById(id);
    }

    @Override
    public void addFilterChain(FilterChainItem filterChainItem) {
        filterChainMapper.insert(filterChainItem);
        initFilterChains(getFilterChainItems());
    }

    @Override
    public void updateFilterChain(FilterChainItem filterChainItem) {
        filterChainMapper.update(filterChainItem);
        initFilterChains(getFilterChainItems());
    }

    @Override
    public void deleteFilterChainById(long id) {
        filterChainMapper.delete(id);
        initFilterChains(getFilterChainItems());
    }

    @Override
    public PageResult<FilterChainItem> getPage(int pageIndex, int pageSize) {
        int startIndex = (pageIndex - 1) * pageSize;
        return PageResult.ok(filterChainMapper.count(), pageIndex, pageSize, filterChainMapper.getPage(startIndex, pageSize));
    }
    public void initFilterChains(List<FilterChainItem> filterChainItems) {
        //1、首先删除以前老的filter，构建默认的过滤器链
        filterChainManager.getFilterChains().clear();
        //2、加载过滤器链
        for (FilterChainItem filterChainItem : filterChainItems) {
            String url = filterChainItem.getUrlPattern();
            String filterName = filterChainItem.getFilterName();
            String[] filterNames = filterName.split(",");
            for (String name : filterNames) {
                //注册所有filter，包含自定义的过滤器
                switch(name){
                    case "anon", "authc", "kicked-out", "jwt-authc":
                        filterChainManager.addToChain(url, name);
                        break;
                    case "roles", "role-or", "jwt-roles":
                        filterChainManager.addToChain(url, name, filterChainItem.getRoles());
                        break;
                    case "perms", "jwt-perms":
                        filterChainManager.addToChain(url, name, filterChainItem.getPermissions());
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
