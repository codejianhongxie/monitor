package com.sequoiadb.monitor.common.spi;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 21:29
 */
public interface Task {

    void execute(Object object);
}
