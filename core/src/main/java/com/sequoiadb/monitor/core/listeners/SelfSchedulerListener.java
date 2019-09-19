package com.sequoiadb.monitor.core.listeners;

import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.TerminalRecord;
import com.sequoiadb.monitor.core.util.ThreadExecutorFactory;
import org.quartz.*;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 22:25
 */
public class SelfSchedulerListener implements SchedulerListener {

    @Override
    public void jobScheduled(Trigger trigger) {

    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {

    }

    @Override
    public void triggerFinalized(Trigger trigger) {

    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {

    }

    @Override
    public void triggersPaused(String triggerGroup) {

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {

    }

    @Override
    public void triggersResumed(String triggerGroup) {

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {

    }

    @Override
    public void jobDeleted(JobKey jobKey) {

    }

    @Override
    public void jobPaused(JobKey jobKey) {

    }

    @Override
    public void jobsPaused(String jobGroup) {

    }

    @Override
    public void jobResumed(JobKey jobKey) {

    }

    @Override
    public void jobsResumed(String jobGroup) {

    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {

    }

    @Override
    public void schedulerInStandbyMode() {

    }

    @Override
    public void schedulerStarted() {

    }

    @Override
    public void schedulerStarting() {

    }

    @Override
    public void schedulerShutdown() {
        try {
            TaskRecordWriteHandler.getInstance().put(new TerminalRecord());
            ThreadExecutorFactory.getInstance().shutdown();
        } catch (InterruptedException e) {
            //
        }
    }

    @Override
    public void schedulerShuttingdown() {

    }

    @Override
    public void schedulingDataCleared() {

    }
}
