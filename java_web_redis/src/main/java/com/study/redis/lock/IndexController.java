package com.study.redis.lock;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.redisson.Redisson;
import org.redisson.RedissonBaseLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@RestController
public class IndexController {

    @Autowired
    private Redisson redisson;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/deduct_stock")
    public String deductStock() {
        String lockKey = "product_101";
        // 设置uuid, 目的是: 自己线程加的锁 被别人删除
        String clientId = UUID.randomUUID().toString();
        RLock redissonLock = redisson.getLock(lockKey);
        try {
            // 方式1 :  使用redis 的setnx  --  jedis.setnx(k,v) 是否设置成功 （是否已经被占用）
            /**
             * 缺点: 系统直接挂了 死锁了
             */
            // Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge");

            /**
             * 优化1 : 加上过期时间
             * 过期时间小于业务时间, 容易被其他线程删除自己锁
             */
            // Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge");
            // 如果执行了上面的方法 然后系统刚好在这行挂掉了, 来不及设置过期时间 会死锁
            // stringRedisTemplate.expire(lockKey, 10, TimeUnit.SECONDS);  // 设置过期时间  - 时间的选择 一定要满足业务执行结束

            // 建议使用
            // Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1",10,TimeUnit.SECONDS);

            /**
             * 优化2: 设置uuid 自己加的锁 自己来删除
             * 缺点: 过期时间小于业务时间,导致加锁没用
             */
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, clientId,10,TimeUnit.SECONDS);

            if (!result) {
                return "error_code";
            }
            //加锁
            redissonLock.lock();  //setIfAbsent(lockKey, clientId, 30, TimeUnit.SECONDS);
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock")); // jedis.get("stock")
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", realStock + ""); // jedis.set(key,value)
                System.out.println("扣减成功，剩余库存:" + realStock);
            } else {
                System.out.println("扣减失败，库存不足");
            }

        } finally {
            // 判断是否是自己加的锁
            if (clientId.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }
            // finally 中用来删除锁
            redissonLock.unlock();
        }

        return "end";
    }
    // <T> RFuture<T> tryLockInnerAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId, RedisStrictCommand<T> command) {
    //
    //     /**
    //      * @param KEYS[1] 传入的key
    //      * @param ARGV[1] 传入的过期时间
    //      * @param ARGV[2] thread + uuid
    //      */
    //     return evalWriteAsync(getRawName(), LongCodec.INSTANCE, command,
    //             "if (redis.call('exists', KEYS[1]) == 0) then " +         // 如果key不存在,
    //                     /**
    //                      * 为什么以下两个命令可以分开写？
    //                      * lua脚本在redis中存在原子性！ == 一条命令
    //                      */
    //                     "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +  // 那么就设置对这个key 进行设置1  hincrby是hash值+指定值的命令
    //                     "redis.call('pexpire', KEYS[1], ARGV[1]); " +     // 给key设置过期时间
    //                     "return nil; " +
    //                     "end; " +
    //                     "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +  // 如果KEYS（getRawName()  传入的key）存在 并且== 1
    //                     "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +             // 那么就设置对这个key 进行设置1  1+1
    //                     "redis.call('pexpire', KEYS[1], ARGV[1]); " +                // 给key再次设置过期时间
    //                     "return nil; " +
    //                     "end; " +
    //                     "return redis.call('pttl', KEYS[1]);",                       // 如果传入的key
    //             Collections.singletonList(getRawName()), unit.toMillis(leaseTime), getLockName(threadId));
    // }
    //
    //
    // /**
    //  * 锁续命逻辑
    //  * @param threadId
    //  * @return
    //  */
    // protected CompletionStage<Boolean> renewExpirationAsync(long threadId) {
    //     /**
    //      * @param KEYS[1] 传入的key
    //      * @param ARGV[1] 传入的过期时间
    //      * @param ARGV[2] thread + uuid
    //      */
    //     return evalWriteAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
    //             "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " + // 如果key存在
    //                     "redis.call('pexpire', KEYS[1], ARGV[1]); " +       // 那么更新过期时间
    //                     "return 1; " +
    //                     "end; " +
    //                     "return 0;",
    //             Collections.singletonList(getRawName()),internalLockLeaseTime, getLockName(threadId));
    // }
    //
    // private void renewExpiration() {
    //     RedissonBaseLock.ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName());
    //     if (ee == null) {
    //         return;
    //     }
    //
    //     Timeout task = commandExecutor.getConnectionManager().newTimeout(new TimerTask() {
    //         @Override
    //         public void run(Timeout timeout) throws Exception {
    //             RedissonBaseLock.ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
    //             if (ent == null) {
    //                 return;
    //             }
    //             Long threadId = ent.getFirstThreadId();
    //             if (threadId == null) {
    //                 return;
    //             }
    //
    //             CompletionStage<Boolean> future = renewExpirationAsync(threadId);
    //             future.whenComplete((res, e) -> {
    //                 if (e != null) {
    //                     log.error("Can't update lock " + getRawName() + " expiration", e);
    //                     EXPIRATION_RENEWAL_MAP.remove(getEntryName());
    //                     return;
    //                 }
    //
    //                 if (res) {
    //                     // reschedule itself
    //                     renewExpiration();  // 根据返回结果 递归执行  从而实现定时任务的效果
    //                 } else {
    //                     cancelExpirationRenewal(null);
    //                 }
    //             });
    //         }
    //     }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);  // 并不是直接开始锁续命 而是30/3  延迟10毫秒 以后
    //
    //     ee.setTimeout(task);
    // }

    @RequestMapping("/redlock")
    public String redlock() {
        String lockKey = "product_001";
        //这里需要自己实例化不同redis实例的redisson客户端连接，这里只是伪代码用一个redisson客户端简化了
        RLock lock1 = redisson.getLock(lockKey);
        RLock lock2 = redisson.getLock(lockKey);
        RLock lock3 = redisson.getLock(lockKey);

        /**
         * 根据多个 RLock 对象构建 RedissonRedLock （最核心的差别就在这里）
         */
        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        try {
            /**
             * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
             * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）
             */
            boolean res = redLock.tryLock(10, 30, TimeUnit.SECONDS);
            if (res) {
                //成功获得锁，在这里处理业务
            }
        } catch (Exception e) {
            throw new RuntimeException("lock fail");
        } finally {
            //无论如何, 最后都要解锁
            redLock.unlock();
        }

        return "end";
    }

    @RequestMapping("/get_stock")
    public String getStock(@RequestParam("clientId") Long clientId) throws InterruptedException {
        String lockKey = "product_stock_101";

        RReadWriteLock readWriteLock = redisson.getReadWriteLock(lockKey);
        RLock rLock = readWriteLock.readLock();

        rLock.lock();
        System.out.println("获取读锁成功：client=" + clientId);
        String stock = stringRedisTemplate.opsForValue().get("stock");
        if (StringUtils.isEmpty(stock)) {
            System.out.println("查询数据库库存为10。。。");
            Thread.sleep(5000);
            stringRedisTemplate.opsForValue().set("stock", "10");
        }
        rLock.unlock();
        System.out.println("释放读锁成功：client=" + clientId);

        return "end";
    }

    @RequestMapping("/update_stock")
    public String updateStock(@RequestParam("clientId") Long clientId) throws InterruptedException {
        String lockKey = "product_stock_101";

        RReadWriteLock readWriteLock = redisson.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();

        writeLock.lock();
        System.out.println("获取写锁成功：client=" + clientId);
        System.out.println("修改商品101的数据库库存为6。。。");
        stringRedisTemplate.delete("stock");
        Thread.sleep(5000);
        writeLock.unlock();
        System.out.println("释放写锁成功：client=" + clientId);

        return "end";
    }

}