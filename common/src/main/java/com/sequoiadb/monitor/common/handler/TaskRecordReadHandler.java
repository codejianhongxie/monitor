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

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 22:16
 */
public class TaskRecordReadHandler implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(TaskRecordReadHandler.class);

    private List<Target<Record>> targets = new ArrayList<>(2);
    private Exchanger<Record> exchanger;

    public TaskRecordReadHandler(Exchanger<Record> exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void run() {

        String outputTypeStr = Configuration.getInstance().getStringProperty(Constants.MONITOR_TARGET_TYPE);
        String[] outputTypeArr = outputTypeStr.split(Constants.RECORD_DELIMIER);
        for(String outputType : outputTypeArr) {
            @SuppressWarnings("unchecked")
            Target<Record> target = (Target<Record>) PluginLoader.getPluginLoader(Target.class).getPlugin(outputType);
            targets.add(target);
        }
        while(true) {
            try {
                Record record = exchanger.get();
                if (record instanceof TerminalRecord) {
                    break;
                }
                for(Target<Record> target: targets) {
                    target.output(record);
                }
            } catch (Exception e) {
                log.error("failed to save record", e);
            }
        }
    }
}
