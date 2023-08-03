package com.rbac.shiro.custom;

import com.rbac.utils.ObjectSerializer;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisSessionDao extends AbstractSessionDAO {
    private final RedissonClient redissonClient;

    private Long expire;

    public Long getExpire() {
        return expire;
    }

    public RedisSessionDao(RedissonClient redissonClient, Long expire) {
        this.redissonClient = redissonClient;
        this.expire = expire;
    }

    /**
     * 创建session
     */
    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        RBucket<String> bucket = redissonClient.getBucket(sessionId.toString());
        if(bucket.get()!=null){
            bucket.set(ObjectSerializer.serialize(session),expire, TimeUnit.MILLISECONDS);
        }
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable serializable) {
        String sessionId = serializable.toString();
        RBucket<String> bucket = redissonClient.getBucket(sessionId);
        return (Session) ObjectSerializer.deserialize(bucket.get());
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        RBucket<String> bucket = redissonClient.getBucket(session.getId().toString());
        bucket.set(ObjectSerializer.serialize(session),expire, TimeUnit.MILLISECONDS);
    }

    @Override
    public void delete(Session session) {
        RBucket<String> bucket = redissonClient.getBucket(session.getId().toString());
        bucket.delete();
    }

    /**
     * 统计活跃session
     * @return
     */
    @Override
    public Collection<Session> getActiveSessions() {
        return Collections.emptySet();
    }
}
