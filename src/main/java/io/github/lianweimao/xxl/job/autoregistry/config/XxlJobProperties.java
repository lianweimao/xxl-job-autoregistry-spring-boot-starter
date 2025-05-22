package io.github.lianweimao.xxl.job.autoregistry.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class XxlJobProperties {
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    @Value("${xxl.job.admin.username}")
    private String adminUsername;
    @Value("${xxl.job.admin.password}")
    private String adminPassword;
    @Value("${xxl.job.executor.appname}")
    private String executorAppname;

    /**
     * 获取第一个调度中心地址
     * @return
     */
    public String getFirstAdminAddress(){
        String address = adminAddresses;
        //如果地址有逗号,则截取第一个
        if (adminAddresses.contains(",")) {
            address = adminAddresses.substring(0, adminAddresses.indexOf(","));
        }
        address = address.trim();
        if(address.endsWith("/")){
            address = address.substring(0, address.length() - 1);
        }
        return address;
    }

}
