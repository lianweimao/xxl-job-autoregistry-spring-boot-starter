package com.xxl.job.autoregistry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xuxueli 2020-10-29 21:11:23
 */
@Getter
@AllArgsConstructor
public enum ScheduleTypeEnum {

    NONE("无"),
    CRON("CRON"),
    FIX_RATE("固定速度"),
    ;

    private String desc;

}
