package com.sequoiadb.monitor.common.constant;

import java.io.File;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 21:23
 */
public class Constants {

    // 基础架构配置参数
    public final static String JOB_ID = "task.id";
    public final static String JOB_GROUP = "task.group";
    public final static String JOB_NAME = "task.name";
    public static final String JOB_CONFIG = "task.config";
    public final static String TYPE = "{TYPE}";
    public final static String ITEM = "{ITEM}";
    public final static String ITEMS = "items";
    public final static String OUTPUT = "output";
    public final static String ARGS = "args";
    public final static String MONITOR_SOURCE_TYPE = "monitor.source.type";
    public final static String MONITOR_SOURCE_TYPE_PREFIX = "monitor.source.type." + TYPE + ".";
    public final static String MONITOR_SOURCE_TYPE_ITEM_CRON = "monitor.source." + TYPE + "." + ITEM + ".cron";
    public final static String MONITOR_SOURCE_TYPE_ITEM_ITEMS = "monitor.source." + TYPE + "." + ITEM +".items";
    public final static String MONITOR_SOURCE_TYPE_ITEM_OUTPUT= "monitor.source." + TYPE + "."+ ITEM + ".output";
    public final static String MONITOR_SOURCE_TYPE_ITEM_ARGS= "monitor.source." + TYPE + "." + ITEM + ".args";
    public final static String MONITOR_SOURCE_TYPE_ITEM_MISFIRE= "monitor.source." + TYPE + "." + ITEM + ".misfire";

    public final static String MONITOR_TARGET_TYPE = "monitor.target.type";

    public final static String ITEM_DELIMITER = ";";
    public final static String RECORD_DELIMIER = ",";
    public final static String ARG_DELIMITER = ":";

    public final static String PROGRAM_DIR = System.getProperty("program.dir");
    public final static String DEFAULT_CONFIG_FILE =  PROGRAM_DIR + "conf" + File.separator + "sysconf.properties";


}
