package com.sequoiadb.monitor.task;

import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.TextRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 10:25
 */
public class TextTask implements Task {

    private Exchanger<Record> exchanger;

    @Override
    public void execute() {

        Record textRecord = new TextRecord("hello");
        try {
            TaskRecordWriteHandler.getInstance().put(textRecord);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
