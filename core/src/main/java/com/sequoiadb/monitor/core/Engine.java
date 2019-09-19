package com.sequoiadb.monitor.core;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.handler.TaskRecordReadHandler;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.TerminalRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.common.util.EncryptUtil;
import com.sequoiadb.monitor.common.util.PluginLoader;
import com.sequoiadb.monitor.core.transport.BufferedRecordExchanger;
import com.sequoiadb.monitor.core.util.SchedulerManager;
import com.sequoiadb.monitor.core.util.ThreadExecutorFactory;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @date 2019/6/23 11:42
 * @version 1.0
 * 1. 支持misfire任务，以当前时间为触发频率立刻触发一次执行，然后以当前时间开始，按照正常的Cron频率依次执行
 */
public class Engine {

    private final static String CONF_OPTION = "conf";
    private final static String P_OPTION = "p";
    private final static String H_OPTION = "h";

    public static void main(String[] args) throws Exception {

        Option configOption = new Option(CONF_OPTION, true, "use given file for configurtion");
        Option pOption = new Option(P_OPTION, true, "encrpyt password");
        Option hOption = new Option(H_OPTION, false, "help");

        Options options = new Options();
        options.addOption(configOption);
        options.addOption(pOption);
        options.addOption(hOption);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            String configFileName = Constants.DEFAULT_CONFIG_FILE;
            if (line.hasOption(CONF_OPTION)) {
                configFileName = line.getOptionValue(CONF_OPTION);
            } else if (line.hasOption(P_OPTION)) {
                String password = line.getOptionValue(P_OPTION);
                EncryptUtil.generateEncryptPassword(password);
                System.exit(0);
            } else if (line.hasOption(H_OPTION)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Options", options);
                System.exit(0);
            }
            final Configuration configuration = Configuration.getInstance();
            configuration.parse(configFileName);

            if (configuration.getStringProperty(Constants.MONITOR_ZK_URL) != null) {
                String zkUrl = configuration.getStringProperty(Constants.MONITOR_ZK_URL);
                int sessionTimeout = configuration.getIntProperty(Constants.MONITOR_ZK_SESSION_TIMEOUT);
                final String watcherPath = configuration.getStringProperty(Constants.MONITOR_ZK_WATCHER_PATH);
                ThreadExecutorFactory.getInstance().submit(new MasterThread(zkUrl, sessionTimeout, watcherPath));
            } else {
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
            throw e;
        }
    }

    public void start(Configuration configuration) {

        Exchanger<Record> exchanger = new BufferedRecordExchanger<Record>();
        SchedulerManager schedulerManager = SchedulerManager.getInstance();
        schedulerManager.start();

        TaskRecordWriteHandler.getInstance().setExchanger(exchanger);
        ThreadExecutorFactory.getInstance().submit(new TaskRecordReadHandler(exchanger));
        List<ScheduleTask> scheduleTaskList = getAllScheduleJob(configuration);

        for(ScheduleTask scheduleTask : scheduleTaskList) {
            schedulerManager.addJob(scheduleTask);
        }
    }

    private List<ScheduleTask> getAllScheduleJob(Configuration configuration) {
        List<ScheduleTask> scheduleTaskList = new LinkedList<>();
        String sourceType = configuration.getStringProperty(Constants.MONITOR_SOURCE_TYPE);
        String sourceTypePrefix = Constants.MONITOR_SOURCE_TYPE_PREFIX.replace(Constants.TYPE, sourceType);
        String[] monitorTypeArr = configuration.getPropertyGroups(sourceTypePrefix);
        for( int id = 0; id < monitorTypeArr.length; id++) {

            String monitorType = monitorTypeArr[id];
            boolean enableMonitor = configuration.getBooleanFromStringProperty(monitorType);
            if (enableMonitor) {

                String monitorTypeName = monitorType.substring(sourceTypePrefix.length());
                Task task;
                if (monitorTypeName.contains(".")) {
                    task = PluginLoader.getPluginLoader(Task.class).getPlugin(monitorTypeName.substring(0, monitorTypeName.lastIndexOf(".")));
                } else {
                    task = PluginLoader.getPluginLoader(Task.class).getPlugin(monitorTypeName);
                }
                String cronExpression = configuration.getStringProperty(
                        Constants.MONITOR_SOURCE_TYPE_ITEM_CRON
                                .replace(Constants.TYPE, sourceType)
                                .replace(Constants.ITEM, monitorTypeName));
                int misFireStatus = configuration.getIntProperty(
                        Constants.MONITOR_SOURCE_TYPE_ITEM_MISFIRE
                                .replace(Constants.TYPE, sourceType)
                                .replace(Constants.ITEM, monitorTypeName)
                );

                Map<String, String> config = new HashMap<>(3);
                config.put(Constants.ITEMS, configuration.getStringProperty(
                        Constants.MONITOR_SOURCE_TYPE_ITEM_ITEMS
                                .replace(Constants.TYPE, sourceType)
                                .replace(Constants.ITEM, monitorTypeName)
                ));
                config.put(Constants.ARGS, configuration.getStringProperty(
                        Constants.MONITOR_SOURCE_TYPE_ITEM_ARGS
                                .replace(Constants.TYPE, monitorType)
                                .replace(Constants.ITEM, monitorTypeName)
                ));
                config.put(Constants.OUTPUT, configuration.getStringProperty(
                        Constants.MONITOR_SOURCE_TYPE_ITEM_OUTPUT
                                .replace(Constants.TYPE, monitorType)
                                .replace(Constants.ITEM, monitorTypeName)
                ));
                ScheduleTask scheduleTask = new ScheduleTask();
                scheduleTask.setConcurrent(false);
                scheduleTask.setJobId(id);
                scheduleTask.setJobName(monitorTypeName + id);
                scheduleTask.setCronExpression(cronExpression);
                scheduleTask.setJobGroup(Constants.JOB_GROUP);
                scheduleTask.setMisfireStatus(misFireStatus);
                scheduleTask.setTask(task);
                scheduleTask.setJobDataMap(config);
                scheduleTaskList.add(scheduleTask);
            }
        }
        return scheduleTaskList;
    }

}
