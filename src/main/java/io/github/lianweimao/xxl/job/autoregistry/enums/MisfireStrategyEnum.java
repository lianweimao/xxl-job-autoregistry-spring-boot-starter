package io.github.lianweimao.xxl.job.autoregistry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MisfireStrategyEnum {

    DO_NOTHING("什么都不做"),
    FIRE_ONCE_NOW("立即执行一次");
    ;
    private String desc;

}
