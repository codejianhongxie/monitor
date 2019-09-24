package com.sequoiadb.monitor.source.consolesource;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.handler.TaskRecordWriteHandler;
import com.sequoiadb.monitor.common.record.DefaultRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Task;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 10:25
 */
public class ConsoleTask implements Task {

    private static Logger log = LoggerFactory.getLogger(ConsoleTask.class);
    private Exchanger<Record> exchanger;

    @Override
    public void execute(Object object) {

        @SuppressWarnings("unchecked")
        Map<String, String> taskConfig = (Map<String,String>)object;
        List<BSONObject> textList = new ArrayList<>();
        textList.add(new BasicBSONObject("key", "Hello World!"));
        DefaultRecord record = new DefaultRecord(taskConfig, textList);
        Random random = new Random();
        try {
            int sleepTime = random.nextInt(10);
            log.info("task Running {}......", sleepTime);
            TimeUnit.SECONDS.sleep(sleepTime);
            TaskRecordWriteHandler.getInstance().put(record);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
