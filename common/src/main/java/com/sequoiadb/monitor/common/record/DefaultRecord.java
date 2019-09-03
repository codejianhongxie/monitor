package com.sequoiadb.monitor.common.record;

import com.sequoiadb.monitor.common.spi.Record;

import java.util.List;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/30 15:43
 */
public class DefaultRecord implements Record {

    private Object taskConfig;
    private List<Object> dataList;

    public Object getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(Object taskConfig) {
        this.taskConfig = taskConfig;
    }

    public List<Object> getDataList() {
        return dataList;
    }

    public void setDataList(List<Object> dataList) {
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
