package com.xxl.job.autoregistry.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import static org.springframework.http.HttpMethod.POST;

@Getter
@AllArgsConstructor
public enum XxlJobApis {

    LOGIN           (POST, "/login"),
    GROUP_PAGE_LIST (POST, "/jobgroup/pageList"),
    GROUP_SAVE      (POST, "/jobgroup/save"),
    GROUP_REMOVE    (POST, "/jobgroup/remove"),
    JOB_PAGE_LIST   (POST, "/jobinfo/pageList"),
    JOB_ADD         (POST, "/jobinfo/add"),
    JOB_UPDATE      (POST, "/jobinfo/update"),
    JOB_START       (POST, "/jobinfo/start"),
    JOB_STOP        (POST, "/jobinfo/stop"),
    JOB_REMOVE      (POST, "/jobinfo/remove"),
    ;
    private HttpMethod method;
    private String path;

}
