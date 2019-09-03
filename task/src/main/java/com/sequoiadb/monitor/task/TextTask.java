package com.sequoiadb.monitor.task;

import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.DefaultRecord;
import com.sequoiadb.monitor.common.record.TextRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 10:25
 */
public class TextTask implements Task {

    private Exchanger<Record> exchanger;

    @Override
    public void execute(Object object) {

        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (Map<String,String>)object;
        List<Object> textList = new ArrayList<>();
        textList.add("hello world!");
        DefaultRecord record = new DefaultRecord();
        record.setTaskConfig(taskConfig);
        record.setDataList(textList);
        try {
            TaskRecordWriteHandler.getInstance().put(record);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
