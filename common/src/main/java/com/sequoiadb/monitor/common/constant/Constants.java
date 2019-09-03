package com.sequoiadb.monitor.common.constant;

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
    public final static String MONITOR_TYPE_PREFIX = "monitor.type.";
    public final static String TYPE = "{TYPE}";
    public final static String MONITOR_TYPE_CRON = "monitor." + TYPE + ".cron";
    public final static String MONITOR_TYPE_ITEMS = "monitor." + TYPE + ".items";
    public final static String MONITOT_TYPE_OUTPUT= "monitor." + TYPE + ".output";
    public final static String MONITOR_TYPE_ARGS= "monitor." + TYPE + ".args";
    public final static String MONITOR_TYPE_MISFIRE= "monitor." + TYPE + ".misfire";

    //sequoiadb 监控项配置参数
    public final static String MONITOR_SOURCE_TYPE = "monitor.source.type";
    public final static String MONITOR_SOURCE_SDB_HOST= "monitor.source.sdb.host";
    public final static String MONITOR_SOURCE_SDB_USER = "monitor.source.sdb.user";
    public final static String MONITOR_SOURCE_SDB_PASSWORD_PRIVATE_KEY = "monitor.source.sdb.password.private_key";
    public final static String MONITOR_SOURCE_SDB_PASSWORD_PUBLIC_KEY = "monitor.source.sdb.password.public_key";
    public final static String MONITOR_SOURCE_SDB_PASSWORD_ENCRYPT_TYPE = "monitor.source.sdb.password.encrypt_type";
    public final static String MONITOR_SOURCE_SDB_PASSWORD = "monitor.source.sdb.password";

    //sequoiadb 数据存储配置参数
    public final static String MONITOR_TARGET_TYPE = "monitor.target.type";
    public final static String MONITOR_TARGET_SDB_DOMAIN = "monitor.target.sdb.domain";
    public final static String MONITOR_TARGET_SDB_CS = "monitor.target.sdb.cs";
    public final static String MONITOR_TARGET_SDB_BACKUP = "monitor.target.sdb.backup";
    public final static String MONITOR_TARGET_SDB_HISTORY_SUFFIX = "monitor.target.sdb.history_suffix";

    public final static String DATA_ROLE = "data";
    public final static String CATA_ROLE = "cata";
    public final static String COORD_ROLE = "coord";
    public final static String NODE_INFO = "node";

    public final static String ITEM_DELIMITER = ";";
    public final static String RECORD_DELIMIER = ",";
    public final static String ARG_DELIMITER = ":";

    public static final String HISTORY = "_his";


}
