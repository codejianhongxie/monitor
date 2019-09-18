package com.sequoiadb.monitor.common.record;

import com.sequoiadb.monitor.common.spi.Record;

import java.util.List;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 10:18
 */
public class TextRecord implements Record {

    private String value;

    public TextRecord(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public List getDataList() {
        return null;
    }
}
