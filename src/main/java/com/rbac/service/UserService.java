package com.rbac.service;


import com.rbac.pojo.User;

import java.util.List;

public interface UserService {
    //用户登录
    User getUserInfoByName(String name);

    List<String> getRoleNames(String name);

    List<String> getPermissionsInfo(List<String> roleNames);

    void removeCache();
}
