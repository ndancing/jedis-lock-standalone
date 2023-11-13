package com.github.example.spring.jedis.lock;

import com.github.example.spring.jedis.lock.enums.JedisConnectionType;
import com.github.example.spring.jedis.lock.exception.JedisLockException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;

abstract class JedisLock implements Lock {

    protected String lockPrefix;
    protected int sleepTime;
    protected JedisConnectionFactory jedisConnectionFactory;
    protected JedisConnectionType connectionType;

    /**
     * get Jedis connection
     *
     * @return
     */
    protected Object getConnection() {
        Assert.notNull(connectionType, "Invalid connection type");
        Assert.notNull(jedisConnectionFactory, "Invalid connection factory");

        Object connection = null;
        if (JedisConnectionType.SINGLE.equals(connectionType)) {
            RedisConnection redisConnection = jedisConnectionFactory.getConnection();
            connection = redisConnection.getNativeConnection();
        } else if (JedisConnectionType.CLUSTER.equals(connectionType)) {
            RedisClusterConnection clusterConnection = jedisConnectionFactory.getClusterConnection();
            connection = clusterConnection.getNativeConnection();
        }
        if (connection == null) {
            throw new JedisLockException("Unable to get Jedis connection");
        }
        return connection;
    }

}
