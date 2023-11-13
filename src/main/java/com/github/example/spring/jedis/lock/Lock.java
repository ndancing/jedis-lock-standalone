package com.github.example.spring.jedis.lock;

public interface Lock {

    /**
     * blocking lock
     *
     * @param key
     * @param request
     * @throws InterruptedException
     */
    void lock(String key, String request) throws InterruptedException;

    /**
     * blocking lock,custom time
     *
     * @param key
     * @param request
     * @param blockTime custom time
     * @return true lock success, false lock fail
     * @throws InterruptedException
     */
    boolean lock(String key, String request, int blockTime) throws InterruptedException;

    /**
     * Non-blocking lock, default timeout: 10s
     *
     * @param key     lock business type
     * @param request value
     * @return true lock success, false lock fail
     */
    boolean tryLock(String key, String request);

    /**
     * Non-blocking lock
     *
     * @param key        lock business type
     * @param request    value
     * @param expireTime custom expireTime
     * @return true lock success, false lock fail
     */
    boolean tryLock(String key, String request, int expireTime);

    /**
     * unlock
     *
     * @param key
     * @param request request must be the same as lock request
     * @return true unlock success, false unlock fail
     */
    boolean unlock(String key, String request);

}
