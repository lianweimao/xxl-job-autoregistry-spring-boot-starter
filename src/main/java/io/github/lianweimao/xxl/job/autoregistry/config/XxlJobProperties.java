package io.github.lianweimao.xxl.job.autoregistry.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class XxlJobProperties {
    /**
     * admin 用户名
     */
    private String adminUsername;
    /**
     * admin 密码
     */
    private String adminPassword;

    /**
     * 调度中心访问地址
     */
    private String adminAddresses;

    /**
     * 执行器应用名
     */
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
