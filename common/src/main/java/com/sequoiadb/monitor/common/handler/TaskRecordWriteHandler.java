package com.sequoiadb.monitor.common.handler;

import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 22:19
 */
public class TaskRecordWriteHandler {

    private Exchanger<Record> exchanger;

    private TaskRecordWriteHandler() {}

    private static class InstanceHolder {
        private final static TaskRecordWriteHandler Instance = new TaskRecordWriteHandler();
    }

    public static TaskRecordWriteHandler getInstance() {
        return InstanceHolder.Instance;
    }

    public void setExchanger(Exchanger<Record> exchanger) {
        this.exchanger = exchanger;
    }

    public void put(Record record) throws InterruptedException {
        exchanger.put(record);
    }

    public boolean existExchanger() {
        return exchanger != null;
    }
}
