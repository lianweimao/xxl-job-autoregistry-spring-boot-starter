package io.github.lianweimao.xxl.job.autoregistry.config;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.xxl.job.core.executor.XxlJobExecutor;
import io.github.lianweimao.xxl.job.autoregistry.api.XxlJobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"xxl.job.admin.username","xxl.job.admin.password"})
@ConditionalOnBean(XxlJobExecutor.class)
@ComponentScan(basePackages = {"io.github.lianweimao.xxl.job.autoregistry"})
public class XxlJobExecutorAutoRegistryConfig {

    @Bean
    public XxlJobProperties xxlJobProperties(XxlJobExecutor xxlJobExecutor){
        XxlJobProperties xxlJobProperties = new XxlJobProperties();
        xxlJobProperties.setAdminAddresses((String) ReflectUtil.getFieldValue(xxlJobExecutor, "adminAddresses"));
        xxlJobProperties.setExecutorAppname((String) ReflectUtil.getFieldValue(xxlJobExecutor, "appname"));
        xxlJobProperties.setAdminUsername(SpringUtil.getProperty("xxl.job.admin.username"));
        xxlJobProperties.setAdminPassword(SpringUtil.getProperty("xxl.job.admin.password"));
        return xxlJobProperties;
    }

    @Bean
    public XxlJobService xxlJobService(XxlJobProperties xxlJobProperties){
        return new XxlJobService(xxlJobProperties);
    }

    @Bean
    public AutoRegistryWorker autoRegistryWorker(XxlJobService xxlJobService,XxlJobProperties xxlJobProperties){
        return new AutoRegistryWorker(xxlJobService,xxlJobProperties);
    }

}
