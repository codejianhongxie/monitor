package com.sequoiadb.monitor.source.sequoiadbsource;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.DefaultRecord;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.source.sequoiadbsource.util.SdbConnectionUtil;
import com.sequoiadb.monitor.source.sequoiadbsource.util.SequoiadbUtil;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/9/24 23:00
 */
public class SdbTableScanTask implements Task {

    private static Logger log = LoggerFactory.getLogger(SdbSessionTask.class);
    private int connectTimeout = 100;
    private int socketTimeout = 1000;
    private List<String> includeHostList;

    @Override
    public void execute(Object object) {

        log.info("begin to collect table scan info.");
        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (HashMap<String,String>)object;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        String args = taskConfig.get(Constants.ARGS);
        String items = taskConfig.get(Constants.ITEMS);
        parseArgs(args);
        parseItems(items);

        DBCursor tableScanCursor = null;
        List<BSONObject> tableScanList = new ArrayList<>(10);
        Sequoiadb db = null;
        try {
            BSONObject matcherCondition = new BasicBSONObject();
            matcherCondition.put("Contexts.Type", "DATA");
            matcherCondition.put("Contexts.Description", new BasicBSONObject("$regex", ".*ScanType:TBSCAN.*"));
            db = SdbConnectionUtil.getInstance().getSdbConnection(connectTimeout, socketTimeout);
            tableScanCursor = db.getSnapshot(Sequoiadb.SDB_SNAP_CONTEXTS, matcherCondition, null, null);
            while (tableScanCursor.hasNext()) {
                BSONObject tableScanContext = tableScanCursor.getNext();
                String nodeName = (String)tableScanContext.get("NodeName");
                BasicBSONList contexts = (BasicBSONList) tableScanContext.get("Contexts");
                for(int i = 0; i < contexts.size(); i++) {
                    BSONObject context = (BSONObject)contexts.get(i);
                    String description = (String)context.get("Description");
                    int collectionNameBeginIndex = description.indexOf("Collection:");
                    if (collectionNameBeginIndex != -1) {
                        int collectionNameEndIndex = description.lastIndexOf(",Matcher");
                        String collectionName =
                                description.substring(collectionNameBeginIndex+"Collection:".length(), collectionNameEndIndex);
                        int matcherBeginIndex = description.lastIndexOf("Matcher:");
                        int matcherEndIndex = description.lastIndexOf(",ScanType");
                        String matcher = description.substring(matcherBeginIndex + "Matcher:".length(), matcherEndIndex);
                        BSONObject tableScan = new BasicBSONObject();
                        tableScan.put("node_name", nodeName);
                        tableScan.put("monitor_time", currentDate);
                        tableScan.put("cl_name", collectionName);
                        tableScan.put("matcher", matcher);
                        tableScanList.add(tableScan);
                    } else {
                        log.error("cannot to parse info: {}", description);
                    }

                }
            }
        } catch (BaseException e) {
            log.error("failed to count context.", e);
        } finally {
            if (null != tableScanCursor) {
                tableScanCursor.close();
            }
            SdbConnectionUtil.getInstance().close(db);
        }
        Record record = new DefaultRecord(taskConfig, tableScanList);
        try {
            TaskRecordWriteHandler.getInstance().put(record);
        } catch (InterruptedException e) {
            log.error("failed to put table scan record into exchanger");
        }
        log.info("finish to collect table scan info");
    }

    private void parseItems(String items) {
    }

    private void parseArgs(String args) {

        String include = "include:";
        String connectTimeout = "connecttimeout:";
        String socketTimeout = "sockettimeout:";
        if (args != null && args.length() > 0) {
            String[] argsStrArr = args.split(Constants.ITEM_DELIMITER);
            for(String arg : argsStrArr) {

                if (arg.startsWith(include)) {
                    String includeHosts = arg.substring(include.indexOf(include) + include.length());
                    includeHostList = Arrays.asList(includeHosts.split(Constants.RECORD_DELIMIER));
                } else if (arg.startsWith(connectTimeout)) {
                    this.connectTimeout = Integer.parseInt(
                            arg.substring(connectTimeout.indexOf(include) + connectTimeout.length()));
                } else if (arg.startsWith(socketTimeout)) {
                    this.socketTimeout = Integer.parseInt(
                            arg.substring(socketTimeout.indexOf(include) + socketTimeout.length()));
                }
            }
        } else {
            includeHostList = SequoiadbUtil.getNotCoordNodes();
        }
    }
}
