package com.study.redis.jedis;

import redis.clients.jedis.Jedis;

/**
 * @ClassName: JedisDemo
 * @Description:
 * @Author: jay
 * @Date: 2022/7/16 16:37
 **/
public class JedisDemo {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("1.116.114.133", 6379);

        String ping = jedis.ping();
        System.out.println("连接成功：" + ping);

        // 测试api
        JedisApiDemo jedisApiDemo = new JedisApiDemo();
        // keys
        jedisApiDemo.keysApiTest(jedis);
        // string
        jedisApiDemo.stringApiTest(jedis);
        // list
        jedisApiDemo.listApiTest(jedis);
        // set
        jedisApiDemo.setApiTest(jedis);
        // hash
        jedisApiDemo.hashApiTest(jedis);
        // zset
        jedisApiDemo.zsetApiTest(jedis);

        jedis.close();
    }
}
