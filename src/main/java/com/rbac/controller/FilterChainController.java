package com.rbac.controller;

import com.rbac.common.PageResult;
import com.rbac.common.Result;
import com.rbac.pojo.FilterChainItem;
import com.rbac.service.FilterChainService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/filters")
public class FilterChainController {
    private final FilterChainService filterChainService;

    public FilterChainController(FilterChainService filterChainService) {
        this.filterChainService = filterChainService;
    }

    @GetMapping("/page")
    public PageResult<FilterChainItem> listFilterChain(Integer pageIndex, Integer pageSize) {
        return filterChainService.getPage(pageIndex, pageSize);
    }


    @GetMapping("/{id}")
    public Result<FilterChainItem> getFilterChain(@PathVariable("id") Long id) {
        return Result.ok(filterChainService.getFilterChainById(id));
    }


    @PostMapping
    public Result<FilterChainItem> saveFilterChain(@RequestBody FilterChainItem filterChainItem) {
        filterChainService.addFilterChain(filterChainItem);
        return Result.ok();
    }


    @PutMapping
    public Result<FilterChainItem> updateFilterChain(@RequestBody FilterChainItem filterChainItem) {
        filterChainService.updateFilterChain(filterChainItem);
        return Result.ok();
    }


    @DeleteMapping("/{id}")
    public Result<FilterChainItem> deleteFilterChain(@PathVariable Long id) {
        filterChainService.deleteFilterChainById(id);
        return Result.ok();
    }

}
