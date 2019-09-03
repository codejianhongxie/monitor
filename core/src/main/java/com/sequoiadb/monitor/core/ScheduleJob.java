package com.sequoiadb.monitor.core;

import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;

import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 22:05
 */
public class ScheduleJob {

    private long jobId;
    private int misfireStatus;
    private String jobGroup;
    private String jobName;
    private String jobStatus;
    private String cronExpression;
    private Task task;
    private boolean isConcurrent;
    private Map<String, String> jobDataMap;

    public Map<String, String> getJobDataMap() {
        return jobDataMap;
    }

    public void setJobDataMap(Map<String, String> jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getMisfireStatus() {
        return misfireStatus;
    }

    public void setMisfireStatus(int misfireStatus) {
        this.misfireStatus = misfireStatus;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isConcurrent() {
        return isConcurrent;
    }

    public void setConcurrent(boolean concurrent) {
        isConcurrent = concurrent;
    }
}
