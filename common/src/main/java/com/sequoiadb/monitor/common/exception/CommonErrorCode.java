package com.sequoiadb.monitor.common.exception;

import com.sequoiadb.monitor.common.spi.ErrorCode;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 8:29
 */
public enum CommonErrorCode implements ErrorCode {
    // 解析参数配置错误
    CONFIG_ERROR("common-01", "解析参数配置失败"),
    SCHEDULE_ERROR("common-02", "调度任务失败"),
    PLUGIN_ERROR("common-03", "插件加载失败"),
    ILLEGUAL_ARGUMENT("common-04","非法参数");

    private final String errorCode;
    private final String description;

    CommonErrorCode(String errorCode, String description) {
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
