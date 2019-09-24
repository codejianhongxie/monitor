package com.sequoiadb.monitor.core.util;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.exception.CommonErrorCode;
import com.sequoiadb.monitor.common.exception.MonitorException;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.core.ScheduleTask;
import com.sequoiadb.monitor.core.TaskEngine;
import com.sequoiadb.monitor.core.TaskEngineDisallowConcurrentExecution;
import com.sequoiadb.monitor.core.listeners.SelfJobListener;
import com.sequoiadb.monitor.core.listeners.SelfSchedulerListener;
import com.sequoiadb.monitor.core.listeners.SelfTriggerListener;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 21:36
 */
public class SchedulerManager {

    private final static Logger log = LoggerFactory.getLogger(SchedulerManager.class);

    private Scheduler server;

    private SchedulerManager() {
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        try {
            server = stdSchedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            log.error("failed to init scheduler", e);
            System.exit(-1);
        }
    }

    private static class InstanceHolder {
        private final static SchedulerManager Instance = new SchedulerManager();
    }

    public static SchedulerManager getInstance() {
        return InstanceHolder.Instance;
    }

    public Scheduler getScheduler() {
        return server;
    }

    public void start() throws MonitorException {
        try {
            server.getListenerManager().addSchedulerListener(new SelfSchedulerListener());
            server.start();
        } catch (SchedulerException e) {
            log.error("failed to start scheduler", e);
            throw MonitorException.asMonitorException(CommonErrorCode.SCHEDULE_ERROR, "failed to start scheduler", e);
        }
    }

    public void addJob(ScheduleTask job) {

        String jobName = job.getJobName();
        String jobGroup = job.getJobGroup();
        Task task = job.getTask();
        String cronExpression = job.getCronExpression();
        int misfireStatus = job.getMisfireStatus();
        boolean isConcurrent = job.isConcurrent();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        CronTrigger trigger = null;
        try {
            trigger = (CronTrigger) server.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            log.error("failed to get trigger.", e);
            return;
        }

        if (null == trigger) {

            Class taskEngineClass = TaskEngineDisallowConcurrentExecution.class;
            if (isConcurrent) {
                taskEngineClass = TaskEngine.class;
            }
            BSONObject taskConfig = new BasicBSONObject();

            JobDataMap data = new JobDataMap();
            data.put(Constants.JOB_NAME, task);
            data.put(Constants.JOB_CONFIG, job.getJobDataMap());
            @SuppressWarnings("unchecked")
            JobDetail jobAdapter = JobBuilder.newJob(taskEngineClass)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData(data)
                    .build();
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            switch (misfireStatus) {
                case CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:
                    cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                    break;
                case CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:
                    cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
                    break;
                default:
                    cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                    break;
            }
            trigger = TriggerBuilder.newTrigger().withSchedule(cronScheduleBuilder)
                    .startNow()
                    .build();
            try {
                server.scheduleJob(jobAdapter, trigger);
            } catch (SchedulerException e) {
                log.error("failed to schedule job [{}]", jobName);
            }
        } else {
            // Trigger已存在，那么更新相应的定时设置
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
                    .cronSchedule(cronExpression);

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            try {
                server.rescheduleJob(triggerKey, trigger);
            } catch (SchedulerException e) {
                log.error("failed to reschedule job [{}]", jobName);
            }
        }
    }

    public void deleteJob(ScheduleTask scheduleTask) throws SchedulerException {

        JobKey jobKey = JobKey.jobKey(scheduleTask.getJobName(),
                scheduleTask.getJobGroup());
        server.deleteJob(jobKey);
    }

    public void runJobNow(ScheduleTask scheduleTask) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(scheduleTask.getJobName(),
                scheduleTask.getJobGroup());
        server.triggerJob(jobKey);
    }

    public void shutdown() throws SchedulerException {
        server.shutdown();
    }



}
