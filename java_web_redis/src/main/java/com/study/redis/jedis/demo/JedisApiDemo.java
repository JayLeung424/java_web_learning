package com.study.redis.jedis.demo;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * @ClassName: JedisUtils
 * @Description:
 * @Author: jay
 * @Date: 2022/7/16 16:40
 **/
public class JedisApiDemo {

    public void keysApiTest(Jedis jedis){
        // 先插入数据
        jedis.set("k1","v1");
        jedis.set("k2","v2");
        jedis.set("k3","v3");
        jedis.set("k4","v4");
        // 拿到所有的key
        Set<String> keys = jedis.keys("*");
        // 输出key的长度
        System.out.println("长度: " + keys.size());
        for (String key : keys) {
            System.out.println(key);
        }
        // 判断key是否存在
        System.out.println(jedis.exists("k1"));
        // 过期时间
        System.out.println(jedis.ttl("k1"));
        // 获取值
        System.out.println(jedis.get("k1"));
    }
    public void stringApiTest(Jedis jedis){
        // 塞入多个值
        jedis.mset("str1","v1","str2","v2","str3","v3");
        // 获取多个值
        System.out.println(jedis.mget("str1", "str2", "str3"));

        // 设置过期时间
        jedis.setex("k5",1000,"v5");
    }
    public void listApiTest(Jedis jedis){
        // 往左边插入多个值
        jedis.lpush("mylist","l1","l2");
        // 往右边插入多个值
        jedis.rpush("mylist","l0");
        List<String> list = jedis.lrange("mylist",0,-1);
        for (String element : list) {
            System.out.println(element);
        }
        // 从左边拿出一个值
        System.out.println(jedis.lpop("mylist"));
    }
    public void setApiTest(Jedis jedis){
        // 插入值
        jedis.sadd("s1","v1","v2");
        // 取出所有值
        Set<String> smembers = jedis.smembers("s1");
        for (String smember : smembers) {
            System.out.println(smember);
        }
        // 判断v是否在k中
        Boolean exist = jedis.sismember("s1", "v1");
        // 删除
        jedis.srem("s1","v2");
    }
    public void hashApiTest(Jedis jedis){
        // 插入值
        jedis.hset("hs1","id","1");
        jedis.hset("hs1","name","zhangsan");

        // 判断是否存在
        Boolean hexists = jedis.hexists("hs1", "name");

        // 所有的field
        Set<String> keys = jedis.hkeys("hs1");
        for (String key : keys) {
            System.out.println(key);
        }
        // 所有的values
        List<String> vals = jedis.hvals("hs1");
        for (String val : vals) {
            System.out.println(val);
        }
    }
    public void zsetApiTest(Jedis jedis){
        // 插入数据
        jedis.zadd("zs1",200,"java");
        // 返回列表
        Set<String> values = jedis.zrange("zs1", 0, -1);
        for (String value : values) {
            System.out.println(value);
        }
    }
}
