package com.rbac.mapper;

import com.rbac.pojo.FilterChainItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface FilterChainMapper {
    List<FilterChainItem> getList();
    FilterChainItem selectById(long id);
    void insert(FilterChainItem filterChainItem);
    void update(FilterChainItem filterChainItem);
    void delete(long id);

    List<FilterChainItem> getPage(int startIndex, int pageSize);
    long count();

}
