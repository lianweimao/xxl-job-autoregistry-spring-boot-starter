package com.xxl.job.autoregistry.config;

import cn.hutool.extra.spring.SpringUtil;
import com.xxl.job.autoregistry.api.XxlJobService;
import com.xxl.job.autoregistry.annotation.XxlJobAutoRegistry;
import com.xxl.job.autoregistry.api.dto.XxlJobGroupDTO;
import com.xxl.job.autoregistry.api.dto.XxlJobInfoDTO;
import com.xxl.job.autoregistry.api.vo.XxlJobGroupVO;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class AutoRegistryWorker {

    private final XxlJobService xxlJobService;
    private final XxlJobProperties xxlJobProperties;

    @EventListener(ApplicationStartedEvent.class)
    public void afterSingletonsInstantiated(ApplicationStartedEvent appStartedEvent) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                //等待JOB服务启动就绪后,再执行任务注册.
                waitXxlJobReady(120);
                doJobRegistry();
            } catch (Exception e) {
                log.error("更新系统任务发生错误:{}", e.getMessage(), e);
            }
        });
        executorService.shutdown();
    }

    private void waitXxlJobReady(int timeoutSeconds) {
        String host = xxlJobProperties.getFirstAdminAddress();
        Long startTime = System.currentTimeMillis();
        while (true) {
            if (isDomainAccessible(host)) {
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                return;
            }
            if (timeoutSeconds > 0 && System.currentTimeMillis() - startTime > timeoutSeconds * 1000) {
                throw new IllegalStateException("JOB服务持续[%d]秒没有就绪".formatted(timeoutSeconds));
            }
        }
    }


    private void doJobRegistry() {
        ApplicationContext applicationContext = SpringUtil.getApplicationContext();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        // 添加执行器
        String appname = xxlJobProperties.getExecutorAppname();
        XxlJobGroupVO xxlJobGroup = xxlJobService.findGroupOrSaveIfNotExists(new XxlJobGroupDTO().setAppname(appname).setTitle(appname));
        List<XxlJobInfoDTO> systemJobList = new ArrayList<>();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            Map<Method, XxlJob> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(), (MethodIntrospector.MetadataLookup<XxlJob>) method -> AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
            } catch (Throwable ex) {
            }
            if (annotatedMethods == null || annotatedMethods.isEmpty()) {
                continue;
            }

            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();
                if (xxlJob == null || !executeMethod.isAnnotationPresent(XxlJobAutoRegistry.class)) {
                    continue;
                }
                XxlJobAutoRegistry xxlJobAutoRegistry = executeMethod.getAnnotation(XxlJobAutoRegistry.class);
                // 添加并启动系统任务
                XxlJobInfoDTO xxlJobInfoDTO = new XxlJobInfoDTO()
                        .setJobGroup(xxlJobGroup.getId())
                        .setJobDesc(xxlJobAutoRegistry.desc())
                        .setAuthor("来自%s的定时任务".formatted(appname))
                        .setAlarmEmail(null)
                        .setScheduleType(xxlJobAutoRegistry.scheduleType())
                        .setScheduleConf(xxlJobAutoRegistry.conf())
                        .setMisfireStrategy(xxlJobAutoRegistry.misfireStrategy())
                        .setExecutorRouteStrategy(xxlJobAutoRegistry.routeStrategy())
                        .setExecutorHandler(xxlJob.value())
                        .setExecutorParam(xxlJobAutoRegistry.param())
                        .setExecutorBlockStrategy(xxlJobAutoRegistry.blockStrategy());
                systemJobList.add(xxlJobInfoDTO);
            }
        }
        //执行任务的批量处理
        Map<Boolean, List<XxlJobInfoDTO>> resultMap = xxlJobService.addJobOrUpdate(systemJobList);
        //打印结果
        if(resultMap == null || resultMap.isEmpty()){
            log.info("本次没有新增或修改任何任务.");
        }else{
            List<XxlJobInfoDTO> addList = resultMap.get(true);
            if(addList != null){
                addList.forEach(item -> log.info("新增定时任务[{}],描述[{}],表达式[{}]",item.getExecutorHandler(),item.getJobDesc(),item.getScheduleConf()));
            }
            List<XxlJobInfoDTO> updateList = resultMap.get(false);
            if(updateList != null){
                updateList.forEach(item -> log.info("修改定时任务[{}],修改后的描述[{}],表达式[{}],其他信息请到调度中心查看",item.getExecutorHandler(),item.getJobDesc(),item.getScheduleConf()));
            }
        }
    }


    /**
     * 判断域名是否可以访问
     * @param urlString
     * @return
     */
    public static boolean isDomainAccessible(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 使用 HEAD 请求避免下载内容
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.getResponseCode();
            return true;
        } catch (Exception e) {
            // 捕获异常，表示不可访问
            return false;
        }
    }
}
