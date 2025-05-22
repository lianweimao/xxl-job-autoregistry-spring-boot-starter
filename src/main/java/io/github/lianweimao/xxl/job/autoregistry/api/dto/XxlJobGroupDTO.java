package io.github.lianweimao.xxl.job.autoregistry.api.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XxlJobGroupDTO {

    private String appname;
    private String title;
    private int addressType = 0;        // 执行器地址类型：0=自动注册、1=手动录入

    public String getAppname() {
        return StrUtil.subWithLength(appname,0,64);
    }

    public String getTitle() {
        return StrUtil.subWithLength(title,0,12);
    }
}
