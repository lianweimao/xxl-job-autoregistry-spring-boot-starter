package io.github.lianweimao.xxl.job.autoregistry.api.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class XxlJobGroupVO {

    private int id;
    private String appname;
    private String title;
    private int addressType;        // 执行器地址类型：0=自动注册、1=手动录入
    private String addressList;     // 执行器地址列表，多地址逗号分隔(手动录入)
    private Date updateTime;
    private List<String> registryList;  // 执行器地址列表(系统注册)

}
