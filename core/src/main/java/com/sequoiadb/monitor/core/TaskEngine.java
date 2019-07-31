package com.sequoiadb.monitor.core;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.spi.Task;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/29 19:50
 */
public class TaskEngine implements Job {

    private final static Logger log = LoggerFactory.getLogger(TaskEngine.class);

    public TaskEngine() {}

    private Task task;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        Object taskObj = context.getJobDetail().getJobDataMap().get(Constants.JOB_NAME);
        if (taskObj instanceof Task) {
            Task task = (Task)taskObj;
            System.out.println("execute task");
            task.execute();
        } else {
            log.error("不支持调度该任务类型");
        }
    }
}
