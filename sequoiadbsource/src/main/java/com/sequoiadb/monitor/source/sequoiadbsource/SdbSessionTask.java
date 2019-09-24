package com.sequoiadb.monitor.source.sequoiadbsource;

import com.sequoiadb.base.DBCursor;
import com.sequoiadb.base.Sequoiadb;
import com.sequoiadb.exception.BaseException;
import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.DefaultRecord;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.source.sequoiadbsource.constant.SequoiadbConstants;
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
 * @date 2019/9/24 22:09
 */
public class SdbSessionTask implements Task {

    private static Logger log = LoggerFactory.getLogger(SdbSessionTask.class);
    private static String SDB_2_8_X = "2.8.x";
    private static String SDB_3_2_X = "3.2.x";
    private static String ARGS_INCLUDE = "include:";
    private List<String> includeHostList;


    @Override
    public void execute(Object object) {

        log.info("begin to collect session info.");
        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (HashMap<String,String>)object;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        String args = taskConfig.get(Constants.ARGS);
        String items = taskConfig.get(Constants.ITEMS);
        parseArgs(args);
        parseItems(items);

        Configuration cfg = Configuration.getInstance();

        BasicBSONList notMatcherCondition1 = new BasicBSONList();
        notMatcherCondition1.add(new BasicBSONObject("LastOpInfo", new BasicBSONObject("$regex", "Command.*SNAPSHOT.*")));
        BasicBSONList notMatcherCondition2 = new BasicBSONList();
        notMatcherCondition2.add(new BasicBSONObject("LastOpInfo", new BasicBSONObject("$regex", "Command.*snapshot.*")));
        BasicBSONList andMatcherCondition = new BasicBSONList();
        andMatcherCondition.add(new BasicBSONObject("$not", notMatcherCondition1));
        andMatcherCondition.add(new BasicBSONObject("$not", notMatcherCondition2));

        BSONObject matcherCondition = new BasicBSONObject();
        matcherCondition.put("Type", "Agent");
        matcherCondition.put("$and", andMatcherCondition);

        if (SDB_2_8_X.equals(cfg.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_VERSION))) {
            matcherCondition.put("Global", false);
        } else if (SDB_3_2_X.equals(cfg.getStringProperty(SequoiadbConstants.MONITOR_SOURCE_SDB_VERSION))) {
            BasicBSONList inMatcherCondition = new BasicBSONList();
            inMatcherCondition.addAll(includeHostList);
            matcherCondition.put("NodeName", new BasicBSONObject("$in", inMatcherCondition));
        }

        DBCursor connectCursor = null;
        Map<String, BSONObject> connectCountMap = new HashMap<>(10);
        for(String host : includeHostList) {
            BSONObject nodeConnect = new BasicBSONObject();
            nodeConnect.put("monitor_time", currentDate);
            nodeConnect.put("node_name", host);
            nodeConnect.put("waiting", 0L);
            nodeConnect.put("running", 0L);
            connectCountMap.put(host, nodeConnect);
        }

        for (String host : includeHostList) {
            Sequoiadb db = null;
            try {
                db = SdbConnectionUtil.getInstance().getSdbConnection(host);
                connectCursor = db.getSnapshot(Sequoiadb.SDB_SNAP_SESSIONS, matcherCondition, null, null);
                while (connectCursor.hasNext()) {
                    BSONObject connect = connectCursor.getNext();
                    String status = (String) connect.get("Status");
                    if (connectCountMap.containsKey(host)) {
                        BSONObject nodeConnect = connectCountMap.get(host);
                        if ("Waiting".equals(status)) {
                            long count = (Long) nodeConnect.get("waiting") + 1;
                            nodeConnect.put("waiting", count);
                        }
                        if ("Running".equals(status)) {
                            long count = (Long) nodeConnect.get("running") + 1;
                            nodeConnect.put("running", count);
                        }
                    } else {
                        BSONObject nodeConnect = new BasicBSONObject();
                        nodeConnect.put("monitor_time", currentDate);
                        nodeConnect.put("node_name", host);
                        if ("Waiting".equals(status)) {
                            nodeConnect.put("waiting", 1L);
                            nodeConnect.put("running", 0L);
                        }
                        if ("Running".equals(status)) {
                            nodeConnect.put("running", 1L);
                            nodeConnect.put("waiting", 0L);
                        }
                        connectCountMap.put(host, nodeConnect);
                    }
                }
            } catch (BaseException e) {
                log.error("failed to count session.", e);
            } finally {
                if (null != connectCursor) {
                    connectCursor.close();
                }
                SdbConnectionUtil.getInstance().close(db);
            }
        }
        List<BSONObject> connectCountList = new ArrayList<>(connectCountMap.values());
        Record record = new DefaultRecord(taskConfig, connectCountList);
        try {
            TaskRecordWriteHandler.getInstance().put(record);
        } catch (InterruptedException e) {
            log.error("failed to put connect record into exchanger");
        }
    }

    private void parseItems(String items) {
    }

    private void parseArgs(String args) {

        if (args != null && args.length() > 0) {
            String[] argsStrArr = args.split(Constants.ITEM_DELIMITER);
            for(String arg : argsStrArr) {
                if (arg.startsWith(ARGS_INCLUDE)) {
                    String includeHosts = arg.substring(ARGS_INCLUDE.indexOf(ARGS_INCLUDE) + ARGS_INCLUDE.length());
                    includeHostList = Arrays.asList(includeHosts.split(Constants.RECORD_DELIMIER));
                }
            }
        } else {
            includeHostList = SequoiadbUtil.getCoordNodes();
        }
    }
}
