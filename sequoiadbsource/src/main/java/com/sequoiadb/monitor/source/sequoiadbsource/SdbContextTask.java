package com.sequoiadb.monitor.source.sequoiadbsource;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
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
 * @date 2019/9/24 22:34
 */
public class SdbContextTask implements Task {

    private static Logger log = LoggerFactory.getLogger(SdbSessionTask.class);
    private List<String> includeHostList;
    private int connectTimeout = 100;
    private int socketTimeout = 1000;

    @Override
    public void execute(Object object) {

        log.info("begin to collect context info.");
        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (HashMap<String,String>)object;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        String args = taskConfig.get(Constants.ARGS);
        String items = taskConfig.get(Constants.ITEMS);
        parseArgs(args);
        parseItems(items);

        Map<String, BSONObject> contextCountMap = new HashMap<>(10);
        for(String host : includeHostList) {
            BSONObject nodeContext = new BasicBSONObject();
            nodeContext.put("monitor_time", currentDate);
            nodeContext.put("node_name", host);
            nodeContext.put("context_count", 0L);
            contextCountMap.put(host, nodeContext);
        }

        Sequoiadb db = null;
        DBCursor contextCursor = null;
        try {
            BasicBSONList notCondition = new BasicBSONList();
            notCondition.add(new BasicBSONObject("Contexts.Type", "DUMP"));
            BSONObject matcherCondition = new BasicBSONObject();
            matcherCondition.put("$not", notCondition);
            log.info("get sdb connection");
            db = SdbConnectionUtil.getInstance().getSdbConnection(connectTimeout, socketTimeout);
            contextCursor = db.getSnapshot(Sequoiadb.SDB_SNAP_CONTEXTS, matcherCondition, null, null);
            while (contextCursor.hasNext()) {
                BSONObject context = contextCursor.getNext();
                String nodeName = (String)context.get("NodeName");
                if (contextCountMap.containsKey(nodeName)) {
                    BSONObject nodeContext = contextCountMap.get(nodeName);
                    long count = (Long) nodeContext.get("context_count") + 1;
                    nodeContext.put("context_count", count);
                } else {
                    BSONObject nodeContext = new BasicBSONObject();
                    nodeContext.put("context_count", 1L);
                }
            }
        } catch (Exception e) {
            log.error("failed to count context.", e);
        } finally {
            if (null != contextCursor) {
                contextCursor.close();
            }
            SdbConnectionUtil.getInstance().close(db);
        }
        List<BSONObject> contextCountList = new ArrayList<>(contextCountMap.values());
        Record record = new DefaultRecord(taskConfig, contextCountList);
        try {
            TaskRecordWriteHandler.getInstance().put(record);
        } catch (InterruptedException e) {
            log.error("failed to put context record into exchanger");
        }
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
