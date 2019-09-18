package com.sequoiadb.monitor.common.handler;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.record.TerminalRecord;
import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Target;
import com.sequoiadb.monitor.common.util.Configuration;
import com.sequoiadb.monitor.common.util.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 22:16
 */
public class TaskRecordReadHandler implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(TaskRecordReadHandler.class);

    private Exchanger<Record> exchanger;

    public TaskRecordReadHandler(Exchanger<Record> exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void run() {

        String outputType = Configuration.getInstance().getStringProperty(Constants.MONITOR_TARGET_TYPE);
        @SuppressWarnings("unchecked")
        Target<Record> target = (Target<Record>) PluginLoader.getPluginLoader(Target.class).getPlugin(outputType);
        while(true) {
            try {
                Record record = exchanger.get();
                if (record instanceof TerminalRecord) {
                    break;
                }
                target.output(record);
            } catch (InterruptedException e) {
                log.error("failed to get record from exchanger", e);
            }
        }
    }
}
