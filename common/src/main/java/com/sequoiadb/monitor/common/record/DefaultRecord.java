package com.sequoiadb.monitor.common.record;

import com.sequoiadb.monitor.common.spi.Record;
import org.bson.BSONObject;

import java.util.List;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/30 15:43
 */
public class DefaultRecord implements Record<BSONObject> {

    private Object taskConfig;
    private List<BSONObject> dataList;

    public DefaultRecord(Object taskConfig, List<BSONObject> dataList) {
        this.taskConfig = taskConfig;
        this.dataList = dataList;
    }

    public Object getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(Object taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public List<BSONObject> getDataList() {
        return dataList;
    }

    public void setDataList(List<BSONObject> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "DefaultRecord{" +
                "taskConfig=" + taskConfig +
                ", dataList=" + dataList +
                '}';
    }
}
