package io.github.lianweimao.xxl.job.autoregistry.api;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.lianweimao.xxl.job.autoregistry.api.dto.XxlJobGroupDTO;
import io.github.lianweimao.xxl.job.autoregistry.api.dto.XxlJobInfoDTO;
import io.github.lianweimao.xxl.job.autoregistry.api.util.JsonUtil;
import io.github.lianweimao.xxl.job.autoregistry.api.vo.XxlJobGroupVO;
import io.github.lianweimao.xxl.job.autoregistry.api.vo.XxlJobInfoVO;
import io.github.lianweimao.xxl.job.autoregistry.config.XxlJobProperties;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class XxlJobService implements InitializingBean {

    private final XxlJobProperties xxlJobProperties;
    private String hostUrl;
    // restTemplate
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();
    // token
    private AtomicReference<String> token = new AtomicReference<>();


    @Override
    public void afterPropertiesSet() throws Exception {
       hostUrl = xxlJobProperties.getFirstAdminAddress();
    }

    /**
     * 检查token是否存在
     */
    private void checkToken() {
        if (token.get() == null) {
            login();
        }
    }

    /**
     * 获取cookie
     * @return
     */
    private String getCookie(){
        return "XXL_JOB_LOGIN_IDENTITY=%s".formatted(token.get());
    }


    /**
     * 登录
     */
    private void login(){
        if(token.get() == null){
            String url = hostUrl + XxlJobApis.LOGIN.getPath();
            MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
            param.add("userName",xxlJobProperties.getAdminUsername());
            param.add("password",xxlJobProperties.getAdminPassword());
            ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                    url,
                    XxlJobApis.LOGIN.getMethod(),
                    new HttpEntity<>(param,null),
                    new ParameterizedTypeReference<>() {
                    }
            );
            ReturnT<String> returnBody = Optional.of(exchange)
                    .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                    .map(ResponseEntity::getBody)
                    .orElseThrow(() -> new IllegalStateException("登录失败"));
            if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
                throw new IllegalStateException("登录失败:%s".formatted(returnBody.getMsg()));
            }
            HttpHeaders headers = exchange.getHeaders();
            List<String> vals = headers.get("Set-Cookie");
            String xxlJobLoginIdentity = vals.stream()
                    .filter(item -> item.startsWith("XXL_JOB_LOGIN_IDENTITY"))
                    .map(item -> item.split("\\=")[1])
                    .findFirst()
                    .orElse(null);
            token.compareAndSet(null, xxlJobLoginIdentity);
        }
    }

    /**
     * 查询所有的执行器
     * @return
     */
    public List<XxlJobGroupVO> queryAllGroup() {
        checkToken();
        XxlJobApis api = XxlJobApis.GROUP_PAGE_LIST;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add("start","0");
        param.add("length","100000");
        param.add("appname","");
        param.add("title","");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<String> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        List<XxlJobGroupVO> xxlJobGroupDTOS = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .map(JsonUtil::parse)
                .map(jsonNode -> ((ObjectNode)jsonNode).withArray("data"))
                .map(nodeList -> JsonUtil.toBean(nodeList, new TypeReference<List<XxlJobGroupVO>>(){}))
                .orElseThrow(() -> new IllegalStateException("获取执行器失败"));
        return xxlJobGroupDTOS;
    }

    /**
     * 新增执行器
     * @param param
     */
    public void saveGroup(XxlJobGroupDTO param) {
        checkToken();
        XxlJobApis api = XxlJobApis.GROUP_SAVE;
        String url = hostUrl + api.getPath();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(buildFormData(param),headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("新增执行器失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("新增执行器失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 删除执行器
     * @param groupId 执行器ID
     * @return
     */
    public void removeGroup(int groupId) {
        checkToken();
        XxlJobApis api = XxlJobApis.GROUP_REMOVE;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("id",groupId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("删除执行器失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("删除执行器失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 查询执行器，不存在则新增
     *
     * @param param
     * @return
     */
    public XxlJobGroupVO findGroupOrSaveIfNotExists(XxlJobGroupDTO param){
        String appname = param.getAppname();
        if(appname == null || appname.isBlank()){
            throw new IllegalArgumentException("appname不能为空");
        }
        // 查询执行器
        List<XxlJobGroupVO> xxlJobGroups = queryAllGroup();
        XxlJobGroupVO xxlJobGroup = xxlJobGroups.stream()
                .filter(item -> item.getAppname().equals(appname))
                .findFirst()
                .orElse(null);
        if(xxlJobGroup == null){
            saveGroup(param);
            return findGroupOrSaveIfNotExists(param);
        }else{
            return xxlJobGroup;
        }
    }

    /**
     * 查询所有的执行器
     * @param groupId 执行器ID
     * @return
     */
    public List<XxlJobInfoVO> queryAllJobs(int groupId) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_PAGE_LIST;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("start","0");
        param.add("length","100000");
        param.add("jobGroup", groupId);
        param.add("triggerStatus","-1");
        param.add("jobDesc","");
        param.add("executorHandler","");
        param.add("author","");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        
        ResponseEntity<String> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        List<XxlJobInfoVO> xxlJobGroupDTOS = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .map(JsonUtil::parse)
                .map(jsonNode -> ((ObjectNode)jsonNode).withArray("data"))
                .map(nodeList -> JsonUtil.toBean(nodeList, new TypeReference<List<XxlJobInfoVO>>(){}))
                .orElseThrow(() -> new IllegalStateException("获取任务列表失败"));
        return xxlJobGroupDTOS;
    }

    /**
     * 新增执行器
     * @param param
     * @return jobId
     */
    public int addJob(XxlJobInfoDTO param) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_ADD;
        String url = hostUrl + api.getPath();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(buildFormData(param),headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("新增任务失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("新增任务失败:%s".formatted(returnBody.getMsg()));
        }
        return Integer.parseInt(returnBody.getContent());
    }
    /**
     * 新增执行器
     * @param param
     */
    public void updateJob(XxlJobInfoDTO param) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_UPDATE;
        String url = hostUrl + api.getPath();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(buildFormData(param),headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("修改任务失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("修改任务失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 启动任务
     * @param jobId 任务ID
     * @return
     */
    public void startJob(int jobId) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_START;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("id",jobId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("启动任务失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("启动任务失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 停止任务
     * @param jobId 任务ID
     * @return
     */
    public void stopJob(int jobId) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_STOP;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("id",jobId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("停止任务失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("停止任务失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 删除任务
     * @param jobId 任务ID
     * @return
     */
    public void removeJob(int jobId) {
        checkToken();
        XxlJobApis api = XxlJobApis.JOB_REMOVE;
        String url = hostUrl + api.getPath();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("id",jobId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie",getCookie());
        ResponseEntity<ReturnT<String>> exchange = restTemplate.exchange(
                url,
                api.getMethod(),
                new HttpEntity<>(param,headers),
                new ParameterizedTypeReference<>() {
                }
        );
        ReturnT<String> returnBody = Optional.of(exchange)
                .filter(obj -> obj.getStatusCode().is2xxSuccessful())
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("删除任务失败"));
        if (returnBody.getCode() != ReturnT.SUCCESS_CODE) {
            throw new IllegalStateException("删除任务失败:%s".formatted(returnBody.getMsg()));
        }
    }

    /**
     * 停止并删除任务
     * @param jobId
     */
    public void stopAndRemoveJob(int jobId){
        stopJob(jobId);
        removeJob(jobId);
    }
    /**
     * 新增或变更JOB
     * @param params
     * @return 新增和变更的集合
     *          true: 新增的任务,新增的任务默认为启动状态
     *          false:修改的任务.修改任务不会变更状态.
     */
    public Map<Boolean, List<XxlJobInfoDTO>> addJobOrUpdate(List<XxlJobInfoDTO> params) {
        if (params == null || params.isEmpty()) {
            return Collections.emptyMap();
        }
        //结果集,true为新增/false为修改
        Map<Boolean, List<XxlJobInfoDTO>> resultMap = new HashMap<>();
        params.stream()
                .collect(Collectors.groupingBy(XxlJobInfoDTO::getJobGroup))
                .forEach((jobGroup, jobList) -> {
                    Map<String, XxlJobInfoVO> serverDataMap = queryAllJobs(jobGroup).stream().collect(Collectors.toMap(XxlJobInfoVO::getExecutorHandler, Function.identity()));
                    for (XxlJobInfoDTO param : jobList) {
                        XxlJobInfoVO serverVo = serverDataMap.get(param.getExecutorHandler());
                        if (serverVo == null) {
                            try {
                                int jobId = addJob(param);
                                startJob(jobId);
                                param.setId(jobId);
                                resultMap.compute(true, (k, v) -> Optional.ofNullable(v).orElseGet(ArrayList::new)).add(param);
                            } catch (Exception e) {
                                log.error("新增任务[{}]失败:{}", param.getExecutorHandler(), e.getMessage(), e);
                            }
                        } else {
                            boolean isEquals = equals(param, serverVo);
                            if (!isEquals) {
                                try {
                                    param.setId(serverVo.getId());
                                    updateJob(param);
                                    resultMap.compute(false, (k, v) -> Optional.ofNullable(v).orElseGet(ArrayList::new)).add(param);
                                } catch (Exception e) {
                                    log.error("修改任务[{}]失败:{}", param.getExecutorHandler(), e.getMessage(), e);
                                }
                            }
                        }
                    }
                });
        return resultMap;
    }

    /**
     * 比较参数和vo是否一致
     * @param param
     * @param serverVo
     * @return
     */
    private boolean equals(XxlJobInfoDTO param, XxlJobInfoVO serverVo) {
        boolean equals = true;
        List<String> updateContent = new ArrayList<>();
        if(param == null || serverVo == null){
            equals = false;
        }
        if(!equals(param.getJobDesc(),serverVo.getJobDesc())){
            equals = false;
            updateContent.add("任务描述由[%s]修改为[%s]".formatted(serverVo.getJobDesc(),param.getJobDesc()));
        }
        if(!equals(param.getAuthor(),serverVo.getAuthor())){
            equals = false;
            updateContent.add("负责人由[%s]修改为[%s]".formatted(serverVo.getAuthor(),param.getAuthor()));
        }
        if(!equals(param.getAlarmEmail(),serverVo.getAlarmEmail())){
            equals = false;
            updateContent.add("报警邮件由[%s]修改为[%s]".formatted(serverVo.getAlarmEmail(),param.getAlarmEmail()));
        }
        if(!equals(param.getScheduleType(),serverVo.getScheduleType())){
            equals = false;
            updateContent.add("调度类型由[%s]修改为[%s]".formatted(serverVo.getScheduleType(),param.getScheduleType()));
        }
        if(!equals(param.getScheduleConf(),serverVo.getScheduleConf())){
            equals = false;
            updateContent.add("调度参数由[%s]修改为[%s]".formatted(serverVo.getScheduleConf(),param.getScheduleConf()));
        }
        if(!equals(param.getGlueType(),serverVo.getGlueType())){
            equals = false;
            updateContent.add("运行模式由[%s]修改为[%s]".formatted(serverVo.getGlueType(),param.getGlueType()));
        }
        if(!equals(param.getExecutorHandler(),serverVo.getExecutorHandler())){
            equals = false;
            updateContent.add("JobHandler由[%s]修改为[%s]".formatted(serverVo.getExecutorHandler(),param.getExecutorHandler()));
        }
        if(!equals(param.getExecutorParam(),serverVo.getExecutorParam())){
            equals = false;
            updateContent.add("任务参数由[%s]修改为[%s]".formatted(serverVo.getExecutorParam(),param.getExecutorParam()));
        }
        if(!equals(param.getExecutorRouteStrategy(),serverVo.getExecutorRouteStrategy())){
            equals = false;
            updateContent.add("路由策略由[%s]修改为[%s]".formatted(serverVo.getExecutorRouteStrategy(),param.getExecutorRouteStrategy()));
        }
        if(!equals(param.getChildJobId(),serverVo.getChildJobId())){
            equals = false;
            updateContent.add("子任务ID由[%s]修改为[%s]".formatted(serverVo.getChildJobId(),param.getChildJobId()));
        }
        if(!equals(param.getMisfireStrategy(),serverVo.getMisfireStrategy())){
            equals = false;
            updateContent.add("调度过期策略由[%s]修改为[%s]".formatted(serverVo.getMisfireStrategy(),param.getMisfireStrategy()));
        }
        if(!equals(param.getExecutorBlockStrategy(),serverVo.getExecutorBlockStrategy())){
            equals = false;
            updateContent.add("阻塞处理策略由[%s]修改为[%s]".formatted(serverVo.getExecutorBlockStrategy(),param.getExecutorBlockStrategy()));
        }
        if(!equals(param.getExecutorTimeout(),serverVo.getExecutorTimeout())){
            equals = false;
            updateContent.add("任务超时时间由[%s]修改为[%s]".formatted(serverVo.getExecutorTimeout(),param.getExecutorTimeout()));
        }
        if(!equals(param.getExecutorFailRetryCount(),serverVo.getExecutorFailRetryCount())){
            equals = false;
            updateContent.add("失败重试次数由[%s]修改为[%s]".formatted(serverVo.getExecutorFailRetryCount(),param.getExecutorFailRetryCount()));
        }
        if(!equals(param.getGlueSource(),serverVo.getGlueSource())){
            equals = false;
            updateContent.add("源码由[%s]修改为[%s]".formatted(serverVo.getGlueSource(),param.getGlueSource()));
        }
        if(!equals(param.getGlueRemark(),serverVo.getGlueRemark())){
            equals = false;
            updateContent.add("源码备注由[%s]修改为[%s]".formatted(serverVo.getGlueRemark(),param.getGlueRemark()));
        }
        if(!equals){
            param.setUpdateContent(String.join(",",updateContent));
        }
        return equals;
    }

    private MultiValueMap<String,Object> buildFormData(Object paramObj){
        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        Method[] methods = ReflectUtil.getMethods(paramObj.getClass());
        for (Field field : ReflectUtil.getFieldsDirectly(paramObj.getClass(), true)) {
            //跳过持有@Deprecated注解的属性
            if(field.isAnnotationPresent(Deprecated.class)){
                continue;
            }
            Object fieldValue = null;
            if(field.getType().isEnum()){
                fieldValue = ((Enum) ReflectUtil.getFieldValue(paramObj, field)).name();
            }else{
                String capitalize = StringUtils.capitalize(field.getName());
                Method method = Arrays.stream(methods).filter(item -> item.getName().equals("get" + capitalize) || item.getName().equals("is" + capitalize)).findFirst().orElse(null);
                if(method == null){
                    fieldValue = ReflectUtil.getFieldValue(paramObj, field);
                }else{
                    fieldValue = ReflectUtil.invoke(paramObj, method);
                }
            }
            formData.add(field.getName(), Optional.ofNullable(fieldValue).orElse(""));
        }
        return formData;
    }

    /**
     * 两个对象是否相等
     * @param obj1
     * @param obj2
     * @return
     */
    private <T> boolean equals(T obj1,T obj2){
        if(Objects.equals(obj1,obj2)){
            return true;
        }
        Class<?> clazz = Optional.ofNullable(obj1).orElse(obj2).getClass();
        if(clazz == String.class && StrUtil.isAllBlank((String) obj1,(String)obj2)){
            return true;
        }
        return false;
    }
}
