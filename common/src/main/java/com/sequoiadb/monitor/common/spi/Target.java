package com.sequoiadb.monitor.common.spi;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/29 19:41
 */
public interface Target<E> {

    void output(E e);
}
