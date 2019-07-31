package com.sequoiadb.monitor.common.spi;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 21:24
 */
public interface Exchanger<E> {

    int size();
    void put(E e) throws InterruptedException;
    <E> E get() throws InterruptedException;
    boolean isEmpty();
}
