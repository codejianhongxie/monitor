package com.sequoiadb.monitor.common.util;


import com.sequoiadb.monitor.common.exception.CommonErrorCode;
import com.sequoiadb.monitor.common.exception.MonitorException;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/7 22:17
 */
public final class StringUtil {


    public static void assertNotBlank(String text) {
        if (null == text || 0 == text.trim().length()) {
            throw MonitorException.asMonitorException(CommonErrorCode.ILLEGUAL_ARGUMENT, "字符串不能为空");
        }
    }

    public static void assertNotBlank(String text, String errorMsg) {
        if (null == text || 0 == text.trim().length()) {
            throw MonitorException.asMonitorException(CommonErrorCode.ILLEGUAL_ARGUMENT, errorMsg);
        }
    }

    public static String assertTrimNotBlank(String text) {
        if (null == text || 0 == text.trim().length()) {
            throw new MonitorException(CommonErrorCode.ILLEGUAL_ARGUMENT, "字符串不能为空");
        }

        return text.trim();
    }



}
