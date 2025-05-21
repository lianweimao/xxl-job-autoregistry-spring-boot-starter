package com.xxl.job.autoregistry.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 执行器路由策略枚举
 */
@Getter
@AllArgsConstructor
public enum ExecutorRouteStrategyEnum {

    //以下所有策略除分片广播外,都只会有一个机器执行任务
    FIRST("第一个"),
    LAST("最后一个"),
    ROUND("轮询"),
    RANDOM("随机"),
    CONSISTENT_HASH("一致性HASH"),
    LEAST_FREQUENTLY_USED("最不经常使用"),
    LEAST_RECENTLY_USED("最近最久未使用"),
    FAILOVER("故障转移"),
    BUSYOVER("忙碌转移"),

    //广播触发对应集群中所有机器执行一次任务，同时系统自动传递分片参数；可根据分片参数开发分片任务
    SHARDING_BROADCAST("分片广播");

    private final String desc;

}
