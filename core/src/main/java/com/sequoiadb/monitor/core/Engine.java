package com.sequoiadb.monitor.core;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.exception.MonitorException;
import com.sequoiadb.monitor.common.handler.TaskRecordReadHandler;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.TerminalRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.common.util.PluginLoader;
import com.sequoiadb.monitor.core.transport.BufferedRecordExchanger;
import com.sequoiadb.monitor.core.util.SchedulerManager;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @date 2019/6/23 11:42
 * @version 1.0
 * TODO:
 * 1. 支持高可用
 * 2. 支持misfire任务，以当前时间为触发频率立刻触发一次执行，然后以当前时间开始，按照正常的Cron频率依次执行
 */
public class Engine {

    private final static Logger log = LoggerFactory.getLogger(Engine.class);
    private final static String CONF_OPTION = "conf";
    private final static int ERROR_EXIT_CODE = -1;

    public static void main(String[] args) {

        Option config = new Option(CONF_OPTION, true, "use given file for configurtion");
        Options options = new Options();
        options.addOption(config);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.getOptions().length <= 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Options", options);
                System.exit(ERROR_EXIT_CODE);
            } else if (line.hasOption(CONF_OPTION)) {
                String configFileName = line.getOptionValue(CONF_OPTION);
                Configuration configuration = Configuration.getInstance();
                configuration.parse(configFileName);

                Engine engine = new Engine();
                engine.start(configuration);
            }
        } catch (Exception e) {
            if (TaskRecordWriteHandler.getInstance().existExchanger()) {
                try {
                    TaskRecordWriteHandler.getInstance().put(new TerminalRecord());
                } catch (InterruptedException ex) {
                    //
                }
            }
            log.error("failed to parse argument", e);
            System.exit(ERROR_EXIT_CODE);
        }
    }

    private void start(Configuration configuration) throws MonitorException {

        Exchanger<Record> exchanger = new BufferedRecordExchanger<Record>();
        SchedulerManager schedulerManager = SchedulerManager.getInstance();
        schedulerManager.start();

        TaskRecordWriteHandler.getInstance().setExchanger(exchanger);
        new Thread(new TaskRecordReadHandler(exchanger)).start();
        List<ScheduleJob> scheduleJobList = getAllScheduleJob(configuration);

        for(ScheduleJob scheduleJob : scheduleJobList) {
            schedulerManager.addJob(scheduleJob);
        }
    }

    private List<ScheduleJob> getAllScheduleJob(Configuration configuration) {
        List<ScheduleJob> scheduleJobList = new LinkedList<>();

        String[] monitorTypeArr = configuration.getPropertyGroups(Constants.MONITOR_TYPE_PREFIX);
        for( int id = 0; id < monitorTypeArr.length; id++) {

            String monitorType = monitorTypeArr[id];
            boolean enableMonitor = configuration.getBooleanFromStringProperty(monitorType);
            if (enableMonitor) {

                String monitorTypeName = monitorType.substring(
                        monitorType.indexOf(Constants.MONITOR_TYPE_PREFIX) + Constants.MONITOR_TYPE_PREFIX.length());
                Task task;
                if (monitorTypeName.contains(".")) {
                    task = PluginLoader.getPluginLoader(Task.class).getPlugin(monitorTypeName.substring(0, monitorTypeName.lastIndexOf(".")));
                } else {
                    task = PluginLoader.getPluginLoader(Task.class).getPlugin(monitorTypeName);
                }
                String cronExpression = configuration.getStringProperty(
                        Constants.MONITOR_TYPE_CRON.replace(Constants.TYPE, monitorTypeName));
                int misFireStatus = configuration.getIntProperty(
                        Constants.MONITOR_TYPE_MISFIRE.replace(Constants.TYPE, monitorTypeName));

                Map<String, String> config = new HashMap<>(3);
                config.put("items", configuration.getStringProperty(
                        Constants.MONITOR_TYPE_ITEMS.replace(Constants.TYPE, monitorTypeName)));
                config.put("args", configuration.getStringProperty(
                        Constants.MONITOR_TYPE_ARGS.replace(Constants.TYPE, monitorTypeName)));
                config.put("output", configuration.getStringProperty(
                        Constants.MONITOT_TYPE_OUTPUT.replace(Constants.TYPE, monitorTypeName)));
                ScheduleJob scheduleJob = new ScheduleJob();
                scheduleJob.setConcurrent(false);
                scheduleJob.setJobId(id);
                scheduleJob.setJobName(monitorTypeName + id);
                scheduleJob.setCronExpression(cronExpression);
                scheduleJob.setJobGroup(Constants.JOB_GROUP);
                scheduleJob.setMisfireStatus(misFireStatus);
                scheduleJob.setTask(task);
                scheduleJob.setJobDataMap(config);
                scheduleJobList.add(scheduleJob);
            }
        }
        return scheduleJobList;
    }

}
