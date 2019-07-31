package com.sequoiadb.monitor.common.exception;

import com.sequoiadb.monitor.common.spi.ErrorCode;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/19 10:55
 */
public class MonitorException extends RuntimeException {

    private ErrorCode errorCode;

    public MonitorException(ErrorCode errorCode, String errorMessage) {
        super(errorCode.toString() + " - " + errorMessage);
        this.errorCode = errorCode;
    }

    private MonitorException(ErrorCode errorCode, String errorMessage, Throwable cause) {
        super(errorCode.toString() + " - " + getMessage(errorMessage) + " - " + getMessage(cause), cause);

        this.errorCode = errorCode;
    }

    public static MonitorException asMonitorException(ErrorCode errorCode, String message) {
        return new MonitorException(errorCode, message);
    }

    public static MonitorException asMonitorException(ErrorCode errorCode, String message, Throwable cause) {
        if (cause instanceof MonitorException) {
            return (MonitorException) cause;
        }
        return new MonitorException(errorCode, message, cause);
    }

    public static MonitorException asMonitorException(ErrorCode errorCode, Throwable cause) {
        if (cause instanceof MonitorException) {
            return (MonitorException) cause;
        }
        return new MonitorException(errorCode, getMessage(cause), cause);
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    private static String getMessage(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof Throwable) {
            StringWriter str = new StringWriter();
            PrintWriter pw = new PrintWriter(str);
            ((Throwable) obj).printStackTrace(pw);
            return str.toString();
            // return ((Throwable) obj).getMessage();
        } else {
            return obj.toString();
        }
    }
}
