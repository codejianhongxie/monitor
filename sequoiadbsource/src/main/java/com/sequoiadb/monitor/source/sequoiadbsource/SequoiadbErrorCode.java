package com.sequoiadb.monitor.source.sequoiadbsource;

import com.sequoiadb.monitor.common.spi.ErrorCode;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/6 16:47
 */
public enum  SequoiadbErrorCode implements ErrorCode {

    // cannot to connect to specified node
    CONNECT_ERROR("sequoiadb-01", "连接节点错误"),
    CLOSE_CONNECT_ERROR("sequoiadb-02", "关闭连接错误"),
    INSERT_ERROR("sequoiadb-03", "批量插入数据失败"),
    GET_COLLECTION_SPACE_ERROR("sequoiadb-04","获取集合空间失败" ),
    CL_TRUNCATE_ERROR("sequoiadb-05", "清空集合失败" ),
    GET_COLLECTION_ERROR("sequoiadb-06","获取集合失败" ),
    CREATE_COLLECTION_ERROR("sequoiadb-07", "创建集合失败"),
    GET_LIST_GROUPS_ERROR("sequoiadb-08", "获取复制组列表失败"),
    CREATE_DOMAIN_ERROR("sequoiadb-09", "创建数据域失败"),
    CREATE_COLLECTION_SPACE_ERROR("sequoiadb-10", "创建集合空间失败"),
    GET_NODE_STATUS_ERROR("sequoiadb-11", "获取节点状态失败");

    private final String errorCode;
    private final String description;

    SequoiadbErrorCode(String errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

}
