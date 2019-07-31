package com.sequoiadb.monitor.core.transport;

import com.sequoiadb.monitor.common.spi.Exchanger;
import com.sequoiadb.monitor.common.spi.Record;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/30 21:27
 */
public class BufferedRecordExchanger<E> implements Exchanger<E> {

    private LinkedBlockingQueue<E> queue;
    private AtomicInteger queueSize;

    public BufferedRecordExchanger(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        queueSize = new AtomicInteger(0);
    }

    public BufferedRecordExchanger() {
        this(10);
    }

    @Override
    public int size() {
        return queueSize.get();
    }

    @Override
    public void put(E e) throws InterruptedException {
        queue.put(e);
        queueSize.getAndIncrement();
    }


    @Override
    public <E> E get() throws InterruptedException {

        @SuppressWarnings("unchecked")
        E e = (E)queue.take();
        queueSize.getAndDecrement();
        return e;
    }

    @Override
    public boolean isEmpty() {
        return queueSize.get() == 0;
    }
}
