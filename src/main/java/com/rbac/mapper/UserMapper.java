package com.rbac.mapper;

import com.rbac.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User getUserByName(String name);
    List<String> getRoleNamesByUserName(String name);
    List<String> getPermissionsByRoleNames(List<String> roleNames);
}
