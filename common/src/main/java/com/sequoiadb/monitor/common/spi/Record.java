package com.sequoiadb.monitor.common.spi;

import java.util.List;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/29 19:39
 */
public interface Record<E> {

    @Override
    String toString();

    <E> List<E> getDataList();

}
