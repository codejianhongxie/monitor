package com.sequoiadb.monitor.source.sequoiadbsource;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.exception.MonitorException;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.DefaultRecord;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.source.sequoiadbsource.util.SdbConnectionUtil;
import com.sequoiadb.monitor.source.sequoiadbsource.util.SequoiadbUtil;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/9/3 16:10
 */
public class SdbNodeTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(SdbNodeTask.class);

    private final static String NODE_STATUS = "node_status";
    private final static String NODE_LSN = "node_lsn";
    private final static String NODE_PERF = "node_perf";
    private final static String ARG_NODE_PERF = "node_perf:";
    private boolean nodeStatus = true;
    private boolean nodeLsn = false;
    private boolean nodePerf = false;
    private List<String> perfItems;
    private Map<String, Long> primaryNodeLsn = new HashMap<>(10);

    @Override
    public void execute(Object object) {

        log.info("begin to collect node info.");

        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (HashMap<String,String>)object;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        String args = taskConfig.get(Constants.ARGS);
        String items = taskConfig.get(Constants.ITEMS);
        parseArgs(args);
        parseItems(items);
        List<BSONObject> nodeStatusList = new LinkedList<>();
        Configuration cfg = Configuration.getInstance();
        try {

            List<String> includeNode = SequoiadbUtil.getNotCoordNodes();
            for (String node : includeNode) {
                BSONObject nodeStatusRecord = collectNodeStatus(node, false, currentDate);
                nodeStatusList.add(nodeStatusRecord);
            }
            if (nodeLsn) {
                for(BSONObject nodeStatus : nodeStatusList) {
                    String groupName = (String)nodeStatus.get("groupname");
                    boolean isPrimary = (Boolean)nodeStatus.get("isprimary");
                    boolean serviceStatus = (Boolean)nodeStatus.get("servicestatus");
                    if (!isPrimary && serviceStatus) {
                        long diffLsn = -1;
                        //避免数据组无主的情况
                        if (primaryNodeLsn.containsKey(groupName)) {
                            long primaryLsn = primaryNodeLsn.get(groupName);
                            long currentLsn = (Long)nodeStatus.get("currentlsn");
                            diffLsn = primaryLsn - currentLsn;
                        }
                        nodeStatus.put("difflsn", diffLsn);
                    } else if (serviceStatus){
                        nodeStatus.put("difflsn", 0);
                    } else {
                        nodeStatus.put("difflsn", -1);
                    }
                }
            }
            //协调节点
            List<String> coordNodes = SequoiadbUtil.getCoordNodes();
            for (String node : coordNodes) {
                BSONObject nodeStatusRecord = collectNodeStatus(node, true, currentDate);
                nodeStatusList.add(nodeStatusRecord);
            }
            Record record = new DefaultRecord(taskConfig, nodeStatusList);
            try {
                TaskRecordWriteHandler.getInstance().put(record);
            } catch (InterruptedException e) {
                log.error("failed to put node record into exchanger");
            }
            log.info("finish to collect node info");
        } catch (MonitorException e) {
            log.error("failed to collect node info", e);
        }
    }

    private void parseArgs(String args) {
        if (args != null && args.length() > 0) {
            String[] argsStrArr = args.split(Constants.ITEM_DELIMITER);
            for(String arg : argsStrArr) {
                if (arg.startsWith(ARG_NODE_PERF)
                        && nodePerf
                        && perfItems != null
                        && perfItems.size() <= 0) {
                    perfItems = new LinkedList<>();
                    String argItemStr = arg.substring(arg.indexOf(ARG_NODE_PERF) + ARG_NODE_PERF.length());
                    String[] argItemArr = argItemStr.split(Constants.RECORD_DELIMIER);
                    perfItems.addAll(Arrays.asList(argItemArr));
                }
            }
        }
        if (perfItems == null) {
            perfItems = new LinkedList<>();
            perfItems.add("TotalDataRead");
            perfItems.add("TotalIndexRead");
        }
    }

    private void parseItems(String items) {
        String[] itemsStrArr = items.split(Constants.RECORD_DELIMIER);
        for (String item : itemsStrArr) {
            switch (item) {
                case NODE_STATUS:
                    nodeStatus = true;
                    break;
                case NODE_LSN:
                    nodeLsn = true;
                    break;
                case NODE_PERF:
                    nodePerf = true;
                    break;
                default:
                    log.error("暂不支持该选项");
                    break;
            }
        }
    }

    private BSONObject collectNodeStatus(String node, boolean isCoord, String currentDate) {
        BSONObject nodeStatusRecord = new BasicBSONObject();
        DBCursor cursor = null;
        Sequoiadb db = null;
        try {
            db = SdbConnectionUtil.getInstance().getSdbConnection(node);
            if (isCoord) {
                if (nodeStatus) {
                    nodeStatusRecord.put("nodename", node);
                    nodeStatusRecord.put("groupname", "SYSCoord");
                    nodeStatusRecord.put("isprimary", true);
                    nodeStatusRecord.put("servicestatus", true);
                    nodeStatusRecord.put("status", "Normal");
                    nodeStatusRecord.put("errorinfo", "");
                }
                if (nodeLsn) {
                    nodeStatusRecord.put("currentlsn", -1L);
                    nodeStatusRecord.put("completelsn", -1L);
                    nodeStatusRecord.put("difflsn", 0L);
                }

                if (nodePerf) {
                    for (String item : perfItems) {
                        nodeStatusRecord.put(item.toLowerCase(), 0L);
                    }
                }
            } else {
                //非协调节点
                cursor = db.getSnapshot(Sequoiadb.SDB_SNAP_DATABASE, "", "", "");
                while (cursor.hasNext()) {
                    BSONObject nodeRecord = cursor.getNext();
                    String groupName = (String)nodeRecord.get("GroupName");
                    boolean isPrimary = (boolean)nodeRecord.get("IsPrimary");
                    if (nodeStatus) {
                        nodeStatusRecord.put("nodename", nodeRecord.get("NodeName"));
                        nodeStatusRecord.put("groupname", groupName);
                        nodeStatusRecord.put("isprimary", isPrimary);
                        nodeStatusRecord.put("servicestatus", nodeRecord.get("ServiceStatus"));
                        nodeStatusRecord.put("status", nodeRecord.get("Status"));
                        nodeStatusRecord.put("errorinfo", "");
                    }
                    if (nodeLsn) {
                        BSONObject currentLsn = (BSONObject) nodeRecord.get("CurrentLSN");
                        long completeLsn = (Long) nodeRecord.get("CompleteLSN");
                        long currentLsnOffset = (Long)currentLsn.get("Offset");
                        nodeStatusRecord.put("currentlsn", currentLsnOffset);
                        nodeStatusRecord.put("completelsn", completeLsn);
                        if (isPrimary) {
                            primaryNodeLsn.put(groupName, currentLsnOffset);
                        }
                    }

                    if (nodePerf) {
                        for (String item : perfItems) {
                            nodeStatusRecord.put(item.toLowerCase(), nodeRecord.get(item));
                        }
                    }
                }
            }
        } catch (BaseException e) {
            log.error("failed to get node[{}] database snapshot", node, e);
            nodeStatusRecord.put("nodename", node);
            nodeStatusRecord.put("groupname", SequoiadbUtil.getNodeGroupName(node));
            nodeStatusRecord.put("isprimary", false);
            nodeStatusRecord.put("servicestatus", false);
            nodeStatusRecord.put("status", "Abnormal");
            nodeStatusRecord.put("errorinfo", e.getCause().getMessage());
            if (nodeLsn) {
                nodeStatusRecord.put("currentlsn", -1L);
                nodeStatusRecord.put("completelsn", -1L);
            }
            if (nodePerf) {
                for (String item : perfItems) {
                    nodeStatusRecord.put(item.toLowerCase(), 0L);
                }
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
            SdbConnectionUtil.getInstance().close(db);
            nodeStatusRecord.put("monitortime", currentDate);
        }
        return nodeStatusRecord;
    }
}
