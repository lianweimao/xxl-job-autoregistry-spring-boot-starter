package com.xxl.job.autoregistry.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用分布式锁.
 * 当你的服务器部署方式是分布式场景,可以通过如下注解启用分布式锁.
 * 你需要配置redis
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(XxlJobAutoRegistryAspect.class)
public @interface EnableConcurrentLockForXxlJobAutoRegistry {
}
