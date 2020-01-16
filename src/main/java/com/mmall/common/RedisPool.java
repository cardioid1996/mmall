package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool;  // jedis连接池
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20")); // 最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));  // 最大空闲连接数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));  // 最小空闲连接数
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));  // 在borrow一个jedis实例时，是否要进行验证操作。若为true，则得到的jedis实例一定是可用的
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));  // 在return一个jedis实例时，是否要进行验证操作。若为true，则得到的jedis实例一定是可用的
    private static String ip = PropertiesUtil.getProperty("redis.ip");
    private static Integer port = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));


    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        config.setBlockWhenExhausted(true);  //连接耗尽的时候是否阻塞，false会抛出异常，true阻塞直到超时
        pool = new JedisPool(config, ip, port, 1000*2);   // timeout = 2s
    }

    static {
        // JVM加载RedisPool类的时候就要初始化pool，且只初始化一次
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }


    // 把 Jedis 放回去
    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("lqkey", "lqvalue");
        returnResource(jedis);
        pool.destroy(); //临时调用，销毁连接池中所有连接
        System.out.println("program end");
    }



}
