package com.sequoiadb.monitor.source.sequoiadbsource.util;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.source.sequoiadbsource.constant.SequoiadbConstants;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/6 17:01
 */
public class SequoiadbUtil {

    private final static Logger log = LoggerFactory.getLogger(SequoiadbUtil.class);
    private static Configuration configuration;

    public static List<String> getCoordNodes() {
        initClusterInfo();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> clusterNodeMap =
                (Map<String,Map<String,List<String>>>)configuration.getObjectProperty(SequoiadbConstants.NODE_INFO);
        return clusterNodeMap.get(SequoiadbConstants.COORD_ROLE).get("SYSCoord");
    }

    public static String getNodeGroupName(String node) {

        initClusterInfo();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> clusterNodeMap =
                (Map<String,Map<String,List<String>>>)configuration.getObjectProperty(SequoiadbConstants.NODE_INFO);

        for(String role : clusterNodeMap.keySet()) {
            Map<String, List<String>> groupMap = clusterNodeMap.get(role);
            for(String group : groupMap.keySet()) {
                List<String> nodeList = groupMap.get(group);
                for(String nodeItem : nodeList) {
                    if (nodeItem.equalsIgnoreCase(node)) {
                        return group;
                    }
                }
            }
        }
        return null;
    }

    public static List<String> getNotCoordNodes() {

        initClusterInfo();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> clusterNodeMap =
                (Map<String,Map<String,List<String>>>)configuration.getObjectProperty(SequoiadbConstants.NODE_INFO);

        List<String> clusterNodeList = new LinkedList<>();
        for(String role : clusterNodeMap.keySet()) {
            Map<String, List<String>> groupMap = clusterNodeMap.get(role);
            for(String group : groupMap.keySet()) {
                if (!"SYSCoord".equalsIgnoreCase(group)) {
                    List<String> nodeList = groupMap.get(group);
                    clusterNodeList.addAll(nodeList);
                }
            }
        }
        return clusterNodeList;
    }

    private static Map<String, Map<String, List<String>>> getClusterNodes() {
        log.info("begin to collect cluster node");
        DBCursor cursor = null;
        Map<String, Map<String, List<String>>> clusterNodeMap = new LinkedHashMap<>(10);
        Sequoiadb db = null;
        try {
            db = SdbConnectionUtil.getInstance().getSdbConnection();

            cursor = db.getList(Sequoiadb.SDB_LIST_GROUPS, null, null, null);

            while( cursor.hasNext()) {

                BSONObject record = cursor.getNext();

                int role = (Integer) record.get("Role");
                String nodeRole;
                if (role == 0) {
                    nodeRole = SequoiadbConstants.DATA_ROLE;
                } else if (role == 1) {
                    nodeRole = SequoiadbConstants.COORD_ROLE;
                } else {
                    nodeRole = SequoiadbConstants.CATA_ROLE;
                }
                String groupName = (String) record.get("GroupName");
                BasicBSONList groupList = (BasicBSONList) record.get("Group");
                Map<String, List<String>> groupMap = new LinkedHashMap<>();
                if (clusterNodeMap.containsKey(nodeRole)) {
                    groupMap= clusterNodeMap.get(nodeRole);
                }
                List<String> nodeList = new LinkedList<>();
                for (Object object : groupList) {
                    BSONObject nodeInfo = (BSONObject)object;
                    String hostName = (String) nodeInfo.get("HostName");
                    BasicBSONList serviceList = (BasicBSONList) nodeInfo.get("Service");

                    for(Object serviceObj : serviceList) {
                        BSONObject service = (BSONObject) serviceObj;
                        int type = (Integer) service.get("Type");
                        if (0 == type) {
                            String serviceName = (String)service.get("Name");
                            String nodeName = hostName + ":" + serviceName;
                            nodeList.add(nodeName);
                        }
                    }
                }
                groupMap.put(groupName, nodeList);
                clusterNodeMap.put(nodeRole, groupMap);
            }
        } catch (Exception e) {
            log.error("failed to collect cluster node info.", e);
            throw e;
        } finally {
            if (null != cursor) {
                cursor.close();
            }
            if (null != db) {
                db.close();
            }
        }
        log.info("finish to collect cluster node info");
        return clusterNodeMap;
    }

    private static void initClusterInfo() {
        configuration = Configuration.getInstance();
        if (configuration.getObjectProperty(SequoiadbConstants.NODE_INFO) == null) {
            synchronized (SequoiadbUtil.class) {
                if (configuration.getObjectProperty(SequoiadbConstants.NODE_INFO) == null) {
                    configuration.putObjectProperty(SequoiadbConstants.NODE_INFO, getClusterNodes());
                }
            }
        }
    }
}
