package com.rbac.realm;

import com.rbac.entity.User;
import com.rbac.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * todo 使用缓存
 */
@Component
public class UsernamePasswordRealm extends AuthorizingRealm {
    private final static Logger LOG = LoggerFactory.getLogger(UsernamePasswordRealm.class);

    private final UserService userService;

    public UsernamePasswordRealm(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LOG.info("doGetAuthorizationInfo");
        SimpleAuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();
        String principal = principals.getPrimaryPrincipal().toString();
        List<String> roleNames = userService.getRoleNames(principal);
        List<String> permissionsInfo = userService.getPermissionsInfo(roleNames);
        LOG.info("user {} roles {} permissions {}",principal,roleNames,permissionsInfo);
        authorizationInfo.addRoles(roleNames);
        authorizationInfo.addStringPermissions(permissionsInfo);
        return authorizationInfo;
    }

    /**
     * 获取认证信息
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String name = token.getPrincipal().toString();
        User userInfoByName = userService.getUserInfoByName(name);
        if(userInfoByName!=null){
            return new SimpleAuthenticationInfo(token.getPrincipal(),userInfoByName.getPwd(), ByteSource.Util.bytes("salt"),name);
        }
        return null;
    }
    /**
     * 清除缓存
     */
    @Override
    protected void doClearCache(PrincipalCollection principals) {
        userService.removeCache();
        super.doClearCache(principals);
    }
}
