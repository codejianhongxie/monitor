package com.sequoiadb.monitor.core;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.spi.Task;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 22:21
 */
@DisallowConcurrentExecution
public class TaskEngineDisallowConcurrentExecution implements Job {

    private final static Logger log = LoggerFactory.getLogger(TaskEngineDisallowConcurrentExecution.class);

    public TaskEngineDisallowConcurrentExecution() {
    }

    private Task task;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Object taskObj = dataMap.get(Constants.JOB_NAME);
        Object taskConfig = dataMap.get(Constants.JOB_CONFIG);
        if (taskObj instanceof Task) {
            Task task = (Task)taskObj;
            task.execute(taskConfig);
        } else {
            log.error("不支持调度该任务类型");
        }
    }
}
