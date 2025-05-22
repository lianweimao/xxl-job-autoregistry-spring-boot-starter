package io.github.lianweimao.xxl.job.autoregistry.config;

import io.github.lianweimao.xxl.job.autoregistry.api.XxlJobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"xxl.job.admin.addresses","xxl.job.admin.username","xxl.job.admin.password","xxl.job.executor.appname"})
@ComponentScan(basePackages = {"com.xxl.job.autoregistry"})
public class XxlJobExecutorAutoRegistryConfig {

    @Bean
    public XxlJobProperties xxlJobProperties(){
        return new XxlJobProperties();
    }

    @Bean
    public XxlJobService xxlJobService(XxlJobProperties xxlJobProperties){
        return new XxlJobService(xxlJobProperties);
    }

    @Bean
    public AutoRegistryWorker systemJobUpdate(XxlJobService xxlJobService,XxlJobProperties xxlJobProperties){
        return new AutoRegistryWorker(xxlJobService,xxlJobProperties);
    }

}
