package com.github.example.spring.jedis.lock;

import com.github.example.spring.jedis.lock.enums.JedisConnectionType;
import com.github.example.spring.jedis.lock.exception.JedisLockException;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.Collections;

public final class JedisStandaloneLock extends JedisLock {
    private static final String LOCK_MSG = "OK";
    private static final Long UNLOCK_MSG = 1L;
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    /**
     * Default lock expire time (milliseconds)
     */
    private static final int DEFAULT_EXPIRE_TIME = 10000;
    private String unlockScript;

    private JedisStandaloneLock(Builder builder) {
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        this.connectionType = builder.connectionType;
        this.lockPrefix = builder.lockPrefix;
        this.sleepTime = builder.sleepTime;
        this.unlockScript();
    }

    public static Builder builder(JedisConnectionFactory jedisConnectionFactory, JedisConnectionType connectionType) {
        return new Builder(jedisConnectionFactory, connectionType);
    }

    @Override
    public boolean tryLock(String key, String request) {
        return tryLock(key, request, DEFAULT_EXPIRE_TIME);
    }

    @Override
    public void lock(String key, String request) throws InterruptedException {
        Object connection = getConnection();
        String result;
        for (; ; ) {
            if (connection instanceof Jedis) {
                result = ((Jedis) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
                if (LOCK_MSG.equals(result)) {
                    ((Jedis) connection).close();
                }
            } else {
                result = ((JedisCluster) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
            }
            if (LOCK_MSG.equals(result)) {
                break;
            }
            Thread.sleep(sleepTime);
        }

    }

    @Override
    public boolean lock(String key, String request, int blockTime) throws InterruptedException {
        Object connection = getConnection();
        String result;
        while (blockTime >= 0) {
            if (connection instanceof Jedis) {
                result = ((Jedis) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
                if (LOCK_MSG.equals(result)) {
                    ((Jedis) connection).close();
                }
            } else {
                result = ((JedisCluster) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, DEFAULT_EXPIRE_TIME);
            }

            if (LOCK_MSG.equals(result)) {
                return true;
            }
            blockTime -= sleepTime;
            Thread.sleep(sleepTime);
        }
        return false;
    }

    @Override
    public boolean tryLock(String key, String request, int expireTime) {
        Object connection = getConnection();
        String result;

        if (connection instanceof Jedis) {
            result = ((Jedis) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            ((Jedis) connection).close();
        } else {
            result = ((JedisCluster) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        }

        return LOCK_MSG.equals(result);
    }

    @Override
    public boolean unlock(String key, String request) {
        Object connection = getConnection();
        Object result;
        if (connection instanceof Jedis) {
            result = ((Jedis) connection).eval(unlockScript, Collections.singletonList(lockPrefix + key), Collections.singletonList(request));
            ((Jedis) connection).close();
        } else if (connection instanceof JedisCluster) {
            result = ((JedisCluster) connection).eval(unlockScript, Collections.singletonList(lockPrefix + key), Collections.singletonList(request));
        } else {
            return false;
        }

        return UNLOCK_MSG.equals(result);
    }


    /**
     * load unlock lua script
     */
    private void unlockScript() {
        try {
            unlockScript = ScriptLoader.load("lock/scripts/unlock.lua");
        } catch (IOException e) {
            throw new JedisLockException("Exception when loading script");
        }
    }

    public static class Builder {
        private static final String DEFAULT_LOCK_PREFIX = "lock_";
        private static final int DEFAULT_SLEEP_TIME = 100;
        private JedisConnectionFactory jedisConnectionFactory;
        private JedisConnectionType connectionType;
        private String lockPrefix = DEFAULT_LOCK_PREFIX;
        private int sleepTime = DEFAULT_SLEEP_TIME;

        private Builder(JedisConnectionFactory jedisConnectionFactory, JedisConnectionType connectionType) {
            this.jedisConnectionFactory = jedisConnectionFactory;
            this.connectionType = connectionType;
        }

        public Builder lockPrefix(String lockPrefix) {
            this.lockPrefix = lockPrefix;
            return this;
        }

        public Builder sleepTime(int sleepTime) {
            this.sleepTime = sleepTime;
            return this;
        }

        public JedisStandaloneLock build() {
            return new JedisStandaloneLock(this);
        }

    }
}
