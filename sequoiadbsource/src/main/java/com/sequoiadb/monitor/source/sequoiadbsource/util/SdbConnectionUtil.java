package com.sequoiadb.monitor.source.sequoiadbsource.util;

import com.sequoiadb.base.ConfigOptions;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.exception.MonitorException;
import com.sequoiadb.monitor.source.sequoiadbsource.SequoiadbErrorCode;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.common.util.EncryptUtil;
import com.sequoiadb.monitor.source.sequoiadbsource.constant.SequoiadbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/1 0:09
 */
public class SdbConnectionUtil {

    private final Logger log = LoggerFactory.getLogger(SdbConnectionUtil.class);

    private Map<String, Sequoiadb> connectMap = new ConcurrentHashMap<String, Sequoiadb>(10);
    private Configuration configuration;

    private SdbConnectionUtil() {
        configuration = Configuration.getInstance();
    }

    private static class InstanceHolder {
        private final static SdbConnectionUtil INSTANCE = new SdbConnectionUtil();
    }

    public static SdbConnectionUtil getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Sequoiadb getCoordConnection() {
        String hosts = configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_HOST);
        String[] hostsArr = hosts.split(Constants.RECORD_DELIMIER);
        for (String host : hostsArr) {

            if (connectMap.containsKey(host)) {
                log.info("begin to valid connect [{}]", host);
                Sequoiadb db = connectMap.get(host);
                if (db.isValid()) {
                    return db;
                }
                log.info("finish to valid connect [{}]", host);
            }
            try {
                String[] hostArr = host.split(Constants.ARG_DELIMITER);
                String hostName = hostArr[0];
                int serviceName = Integer.parseInt(hostArr[1]);
                Configuration configuration = Configuration.getInstance();
                String userName = configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_USER);
                String password = EncryptUtil.passwordDecrypt(
                        configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD_ENCRYPT_TYPE),
                        configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD_PRIVATE_KEY),
                        configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD));
                Sequoiadb db = getConnection(hostName, serviceName, userName, password);
                connectMap.put(host, db);
                return db;
            } catch (BaseException e) {
                // ignore exception
            }
        }
        throw MonitorException.asMonitorException(SequoiadbErrorCode.CONNECT_ERROR, "failed to connect sdb");
    }

    public Sequoiadb getConnection(String host, int serviceName, String username, String password) {
        ConfigOptions configOptions = new ConfigOptions();
        configOptions.setConnectTimeout(configuration.getIntProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_CONNECT_TIMEOUT));
        configOptions.setMaxAutoConnectRetryTime(configuration.getIntProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_CONNECT_RETRY));
        configOptions.setSocketTimeout(configuration.getIntProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_SOCKET_TIMEOUT));
        return new Sequoiadb(host, serviceName, username, password, configOptions);
    }

    public Sequoiadb getConnection(String node) {

        if (connectMap.containsKey(node)) {
            Sequoiadb db = connectMap.get(node);
            if (db.isValid()) {
                return db;
            }
            log.info("connect for [{}] is abnormal, retry to connect", node);
            db.close();
            connectMap.remove(node);
        }

        String[] hostArr = node.split(Constants.ARG_DELIMITER);
        String hostName = hostArr[0];
        int serviceName = Integer.parseInt(hostArr[1]);
        Configuration configuration = Configuration.getInstance();
        String userName = configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_USER);
        String password = EncryptUtil.passwordDecrypt(
                configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD_ENCRYPT_TYPE),
                configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD_PRIVATE_KEY),
                configuration.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_PASSWORD));
        log.info("begin to connect [{}]", node);
        Sequoiadb db = getConnection(hostName, serviceName, userName, password);
        log.info("finish to connect [{}]", node);
        connectMap.put(node, db);
        return db;
    }

    public void close() {

        for (String node : connectMap.keySet()) {
            try {
                connectMap.get(node).close();
            } catch (Exception e) {
                log.warn("failed to close connect", e);
            }
        }
    }
}
