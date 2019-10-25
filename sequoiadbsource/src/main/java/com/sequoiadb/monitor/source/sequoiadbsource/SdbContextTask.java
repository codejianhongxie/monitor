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
    private int alarmCount = 100;

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

        List<BSONObject> sessionContextList = new ArrayList<>(10);

        BSONObject matcherCondition = new BasicBSONObject();
        matcherCondition.put("TotalCount", alarmCount);
        matcherCondition.put("Global", false);
        for(String host : includeHostList) {
            Sequoiadb db = null;
            DBCursor contextCursor = null;
            try {
                db = SdbConnectionUtil.getInstance().getSdbConnection(host);
                contextCursor = db.getList(Sequoiadb.SDB_LIST_CONTEXTS, matcherCondition, null, null);
                while (contextCursor.hasNext()) {
                    BSONObject context = contextCursor.getNext();

                    String nodeName = (String) context.get("NodeName");
                    int totalCount =  (Integer)context.get("TotalCount");
                    long sessionId = (Long)context.get("SessionID");

                    BSONObject sessionContext = new BasicBSONObject();
                    sessionContext.put("monitortime", currentDate);
                    sessionContext.put("nodename", nodeName);
                    sessionContext.put("sessionid", sessionId);
                    sessionContext.put("totalcount", totalCount);
                    sessionContextList.add(sessionContext);
                }
            } catch (Exception e) {
                log.error("failed to count context.", e);
            } finally {
                if (null != contextCursor) {
                    contextCursor.close();
                }
                SdbConnectionUtil.getInstance().close(db);
            }
        }
        Record record = new DefaultRecord(taskConfig, sessionContextList);
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
        String alarmCount = "alarmcount:";
        if (args != null && args.length() > 0) {
            String[] argsStrArr = args.split(Constants.ITEM_DELIMITER);
            for(String arg : argsStrArr) {

                if (arg.startsWith(include)) {
                    String includeHosts = arg.substring(include.indexOf(include) + include.length());
                    includeHostList = Arrays.asList(includeHosts.split(Constants.RECORD_DELIMIER));
                } else if (arg.startsWith(connectTimeout)) {
                    this.connectTimeout = Integer.parseInt(
                            arg.substring(arg.indexOf(include) + connectTimeout.length()));
                } else if (arg.startsWith(socketTimeout)) {
                    this.socketTimeout = Integer.parseInt(
                            arg.substring(arg.indexOf(include) + socketTimeout.length()));
                } else if (arg.startsWith(alarmCount)) {
                    this.alarmCount = Integer.parseInt(
                            arg.substring(arg.indexOf(alarmCount) + alarmCount.length()));
                }
            }
        }
        if (includeHostList == null) {
            includeHostList = SequoiadbUtil.getCoordNodes();
        }
    }
}
