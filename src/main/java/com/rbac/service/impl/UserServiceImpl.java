package com.rbac.service.impl;

import com.rbac.pojo.User;
import com.rbac.mapper.UserMapper;
import com.rbac.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    private UserMapper userMapper;

    private final RedissonClient redissonClient;

    public UserServiceImpl(UserMapper userMapper, RedissonClient redissonClient) {
        this.userMapper = userMapper;
        this.redissonClient = redissonClient;
    }

    @Override
    public User getUserInfoByName(String name) {
        RBucket<User> bucket = redissonClient.getBucket("user-info"+getSessionId());
        User cacheUser = bucket.get();
        if(cacheUser!=null){
            return cacheUser;
        }
        User user = userMapper.getUserByName(name);
        bucket.set(user,getExpire(), TimeUnit.MILLISECONDS);
        return user;
    }


    @Override
    public List<String> getRoleNames(String name) {
        RList<String> rList = redissonClient.getList("roles-info"+getSessionId());
        if(!CollectionUtils.isEmpty(rList)){
            return rList;
        }
        List<String> roleNamesByUserName = userMapper.getRoleNamesByUserName(name);
        rList.addAll(roleNamesByUserName);
        rList.expire(Duration.ofMillis(getExpire()));
        return roleNamesByUserName;
    }

    @Override
    public List<String> getPermissionsInfo(List<String> roleNames) {
        RList<String> rList = redissonClient.getList("permissions-info"+getSessionId());
        if(!CollectionUtils.isEmpty(rList)){
            return rList;
        }
        List<String> permissions = userMapper.getPermissionsByRoleNames(roleNames);
        rList.addAll(permissions);
        rList.expire(Duration.ofMillis(getExpire()));
        return permissions;
    }

    @Override
    public void removeCache() {
        redissonClient.getBucket("user-info"+getSessionId()).delete();
        redissonClient.getList("roles-info"+getSessionId()).delete();
        redissonClient.getList("permissions-info"+getSessionId()).delete();
    }

    private static String getSessionId() {
        return SecurityUtils.getSubject().getSession().getId().toString();
    }

    private static Long getExpire() {
        return SecurityUtils.getSubject().getSession().getTimeout();
    }

}
