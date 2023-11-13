package com.github.example.spring.jedis.lock.exception;

import redis.clients.jedis.exceptions.JedisException;

public class JedisLockException extends JedisException {

    private static final long serialVersionUID = -6623246174867986125L;

    public JedisLockException(String message) {
        super(message);
    }

    public JedisLockException(Throwable e) {
        super(e);
    }

    public JedisLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
