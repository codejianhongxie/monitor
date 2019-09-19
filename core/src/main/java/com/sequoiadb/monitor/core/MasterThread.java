package com.sequoiadb.monitor.core;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.util.Configuration;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/9/19 9:08
 */
public class MasterThread implements Closeable, Runnable {

    private static Logger log = LoggerFactory.getLogger(MasterThread.class);

    private ZooKeeper zk;
    private String zkUrl;
    private int sessionTimeout;
    private String masterPath;
    private static boolean isMaster = false;

    public MasterThread(String zkUrl, int sessionTimeout, String masterPath) {
        this.zkUrl = zkUrl;
        this.sessionTimeout = sessionTimeout;
        this.masterPath = masterPath;
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        try {
            zk = new ZooKeeper(zkUrl, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    switch (event.getType()) {
                        case None: {
                            if (!isMaster) {
                                toBeMaster();
                            }
                        }break;
                        case NodeDeleted:{
                            if (masterPath.equals(event.getPath()) && !isMaster) {
                                toBeMaster();
                            }
                        }
                        break;
                        case NodeCreated:
                        case NodeChildrenChanged: {
                            //
                        }
                        break;
                        default:
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            try {
                zk.exists(masterPath, true);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
    }

    private void toBeMaster() {
        Configuration configuration = Configuration.getInstance();
        String watcherPath = configuration.getStringProperty(Constants.MONITOR_ZK_WATCHER_PATH);
        try {
            log.info("try to be a master node");
            zk.create(watcherPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            log.info("finish to be a master node");
            isMaster = true;
            zk.exists(watcherPath, true);
            if (isMaster) {
                Engine engine = new Engine();
                engine.start(configuration);
            }
        } catch (Exception e) {
            log.warn("failed to be a master node", e);
            while(true) {
                try {
                    zk.exists(watcherPath, true);
                    break;
                } catch (Exception ex) {
                    int sleepTime = 3;
                    log.warn("failed to watch node, after {} seconds retry to watch", sleepTime, ex);
                    try {
                        TimeUnit.SECONDS.sleep(sleepTime);
                    } catch (InterruptedException exc) {
                        //
                    }
                }
            }
        }

    }

    @Override
    public void close() throws IOException {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
