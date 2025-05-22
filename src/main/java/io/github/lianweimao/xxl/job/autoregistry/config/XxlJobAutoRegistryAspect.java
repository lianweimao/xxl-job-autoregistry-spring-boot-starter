package io.github.lianweimao.xxl.job.autoregistry.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@Aspect
@Slf4j
public class XxlJobAutoRegistryAspect {

    @Around(value="execution(public * io.github.lianweimao.xxl.job.autoregistry.config.AutoRegistryWorker.afterSingletonsInstantiated(..))")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
        RLock lock = redissonClient.getLock("XxlJobAutoRegistry");
        boolean success = false;
        try {
            success = lock.tryLock(0L, 0L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!success) {
            throw new RuntimeException("获取xxljob自动注册锁失败");
        }
        log.info("获取xxljob自动注册锁成功");
        try {
            Object proceed = pjp.proceed();
            return proceed;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
            }
        }
    }
}
