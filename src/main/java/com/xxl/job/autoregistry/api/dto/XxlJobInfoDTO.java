package com.xxl.job.autoregistry.api.dto;

import cn.hutool.core.util.StrUtil;
import com.xxl.job.autoregistry.api.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.autoregistry.api.enums.MisfireStrategyEnum;
import com.xxl.job.autoregistry.api.enums.ScheduleTypeEnum;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class XxlJobInfoDTO {

	private int id;
	private int jobGroup;		// 执行器主键ID
	private String jobDesc;

	private String author;		// 负责人
	private String alarmEmail;	// 报警邮件
	/**
	 * 调度类型
	 */
	private ScheduleTypeEnum scheduleType = ScheduleTypeEnum.CRON;
	private String scheduleConf;			// 调度配置，值含义取决于调度类型
	/**
	 * 调度过期策略
	 */
	private MisfireStrategyEnum misfireStrategy = MisfireStrategyEnum.DO_NOTHING;
	/**
	 * 执行器路由策略
	 */
	private ExecutorRouteStrategyEnum executorRouteStrategy = ExecutorRouteStrategyEnum.BUSYOVER;
	private String executorHandler;		    // 执行器，任务Handler名称
	private String executorParam;		    // 执行器，任务参数
	/**
	 * 阻塞处理策略
	 * 默认: 串行执行
	 */
	private ExecutorBlockStrategyEnum executorBlockStrategy= ExecutorBlockStrategyEnum.SERIAL_EXECUTION;
	/**
	 * 任务执行超时时间，单位秒
	 * 大于0 生效
	 */
	private int executorTimeout = 0;
	/**
	 * 失败重试次数 大于0生效
	 */
	private int executorFailRetryCount = 0;
	
	private String glueType = "BEAN";		// GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
	private String glueSource;		// GLUE源代码
	private String glueRemark;		// GLUE备注

	private String childJobId;		// 子任务ID，多个逗号分隔

	/**
	 * 使用必须参数构造对象
	 * @param jobGroup
	 * @param jobDesc
	 * @param author
	 * @param executorHandler
	 */
	public XxlJobInfoDTO(int jobGroup, String jobDesc, String author, String scheduleConf,String executorHandler) {
		this.jobGroup = jobGroup;
		this.jobDesc = jobDesc;
		this.author = author;
		this.scheduleConf = scheduleConf;
		this.executorHandler = executorHandler;
	}

	public String getJobDesc() {
		return StrUtil.subWithLength(jobDesc,0,255);
	}

	public String getAuthor() {
		return StrUtil.subWithLength(author,0,64);
	}

	public String getAlarmEmail() {
		return StrUtil.subWithLength(alarmEmail,0,255);
	}

	public String getScheduleConf() {
		return StrUtil.subWithLength(scheduleConf,0,128);
	}

	public String getExecutorHandler() {
		return StrUtil.subWithLength(executorHandler,0,255);
	}

	public String getExecutorParam() {
		return StrUtil.subWithLength(executorParam,0,512);
	}

	public String getGlueType() {
		return StrUtil.subWithLength(glueType,0,50);
	}

	public String getGlueRemark() {
		return StrUtil.subWithLength(glueRemark,0,128);
	}

	public String getChildJobId() {
		return StrUtil.subWithLength(childJobId,0,255);
	}
}
