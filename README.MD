# 使用说明

本插件专注于任务注册和更新。

服务启动后，会通过**异步**的方式进行执行器注册和任务注册。

执行器以`appname`为条件，查询是否存在，若不存在，则以自动注册新建。

任务以`@XxlJob`注解的值，也就是jobhandler作为唯一标识，从调度中心获取数据进行对比。

若任务不存在，则新增并立刻启动。

若任务已存在，则对比其余参数，若不一致，则进行修改。

> 修改时不会修改任务状态

> 若调度中心无法访问，上述工作会等待最多120秒。

## 版本对应关系

| 插件版本 | JDK版本 | SpringBoot版本                | XxlJob版本              |
| -------- | ------- | ----------------------------- | ----------------------- |
| 3.1.0    | 17+     | 3.1.X ~ 3.4.X；更高版本未测试 | <=3.1.0；更高版本未测试 |

## 自动注册任务到调度中心

1. 引入依赖

~~~xml
<dependency>
    <groupId>io.github.lianweimao</groupId>
    <artifactId>xxl-job-autoregistry-spring-boot-starter</artifactId>
    <version>3.1.0</version>
</dependency>
~~~

2. 在你的配置文件中，增加以下配置

> 注意: xxlJob执行器的配置参数，要使用[官方](https://www.xuxueli.com/xxl-job/)标准格式。

~~~properties
# 调度中心用户名[选填],为空时任务不会自动注册
xxl.job.admin.username=admin
# 调度中心密码[选填],为空时任务不会自动注册
xxl.job.admin.password=123456
~~~

3. 在你的定时任务handler上，增加注解`@XxlJobAutoRegistry`即可。默认情况下，只需要最少2个参数就可以完成。

~~~java
@XxlJobAutoRegistry(conf = "* 1 * * * ?",desc = "这是一个demoJob")
@XxlJob("demoJob")
public void demoJob(){
    String jobParam = XxlJobHelper.getJobParam();
    // do job
}
~~~

当然，你可以通过修改注解参数去自定义，支持修改以下参数：

* 调度类型
* 阻塞处理策略
* 路由策略
* 调度过期策略
* 执行参数

## 集群部署的并发控制

如果你的服务是集群部署，在启动类增加`@EnableRedissonLockForXxlJobAutoRegistry`注解，通过分部署锁控制，防止并发更新导致的任务重复问题。开启该功能请确保存在redis相关配置。
~~~java
@SpringBootApplication
@EnableRedissonLockForXxlJobAutoRegistry
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
~~~