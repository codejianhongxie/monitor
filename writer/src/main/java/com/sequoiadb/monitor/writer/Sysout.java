package com.sequoiadb.monitor.writer;

import com.sequoiadb.monitor.common.spi.Record;
import com.sequoiadb.monitor.common.spi.Writer;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 10:16
 */
public class Sysout implements Writer<Record> {

    @Override
    public void output(Record record) {

        System.out.println(record.toString());
    }
}
