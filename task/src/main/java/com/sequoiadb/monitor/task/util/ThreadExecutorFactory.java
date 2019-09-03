package com.sequoiadb.monitor.task.util;

import java.util.concurrent.*;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/9/3 11:42
 */
public class ThreadExecutorFactory {

    private ExecutorService executorService;

    private ThreadExecutorFactory() {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 60000;
        int queueSize = 100;
        executorService =
                new ThreadPoolExecutor(
                        corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(queueSize),
                        new CollectorThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
    }

    private static class InstanceHolder {
        private final static ThreadExecutorFactory instance = new ThreadExecutorFactory();
    }

    public static ThreadExecutorFactory getInstance() {
        return InstanceHolder.instance;
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    public void shutdown() {
        if (executorService != null
                && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
