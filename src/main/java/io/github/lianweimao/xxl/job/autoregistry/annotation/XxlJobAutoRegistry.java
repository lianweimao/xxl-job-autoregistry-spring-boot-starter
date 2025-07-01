package io.github.lianweimao.xxl.job.autoregistry.annotation;

import io.github.lianweimao.xxl.job.autoregistry.enums.ExecutorRouteStrategyEnum;
import io.github.lianweimao.xxl.job.autoregistry.enums.MisfireStrategyEnum;
import io.github.lianweimao.xxl.job.autoregistry.enums.ScheduleTypeEnum;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.handler.annotation.XxlJob;

import java.lang.annotation.*;

/**
 * 该注解与{@link XxlJob}注解配合使用
 * 同时持有两个注解的任务,会被自动管理,注册到调度中心.
 * 以JobHandler为条件
 * 若任务不存在,则会新增并启动.
 * 若任务已存在,会更新任务其他信息,但不会变更任务状态.
 * 而对于执行器下其他任务,则不会修改.
 *
 * @author lianweimao
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJobAutoRegistry {

    /**
     * 当 {@link XxlJobAutoRegistry#scheduleType()} 为 CRON时,此处填 CRON 表达式
     * 定时运行配置 cron表达式在线生成 https://cron.qqe2.com/
     * 一般由7位构成 秒 分 时 日 月 周 年 年可忽略 日和周必须有一个为?,代表不设置
     *
     * 当 {@link XxlJobAutoRegistry#scheduleType()} 为 FIX_RATE时,此处填写间隔时间,单位秒
     */
    String conf();

    /**
     * 任务描述信息
     * 该参数不能为空字符或空,否则添加不成功
     */
    String desc();

    /**
     * 任务负责人
     * 为空时自动填充为 来自XX服务的定时任务
     * @return
     */
    String author() default "";

    /**
     * 报警邮件
     * @return
     */
    String alarmEmail() default "";

    /**
     * 调度类型
     * @return
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.CRON;

    /**
     * 阻塞处理策略
     * 默认为单行串行 无关痛痒的第三方数据拉取类任务,建议切换为:DISCARD_LATER,抛弃后续调度
     */
    ExecutorBlockStrategyEnum blockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 路由策略
     * @return
     */
    ExecutorRouteStrategyEnum routeStrategy() default ExecutorRouteStrategyEnum.BUSYOVER;

    /**
     * 调度过期策略
     * @return
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 执行参数
     * @return
     */
    String param() default "";
}
