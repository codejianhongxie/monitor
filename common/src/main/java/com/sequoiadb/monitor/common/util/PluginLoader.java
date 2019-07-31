package com.sequoiadb.monitor.common.util;


import com.sequoiadb.monitor.common.exception.CommonErrorCode;
import com.sequoiadb.monitor.common.exception.MonitorException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class PluginLoader<T> {

    private static final String SERVICE_DIR = "META-INF/services/";
    private final Class<?> type;

    private PluginLoader(Class<?> type) {
        this.type = type;
    }

    public static <T> PluginLoader<T> getPluginLoader(Class<T> type) {
        return  new PluginLoader<>(type);
    }

//    public Class<? extends T> loadClass(String name) {
//        String fileName = SERVICE_DIR + type.getName();
//        try {
//            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
//            if (null == urls) {
//                throw MonitorException.asMonitorException(CommonErrorCode.PLUGIN_ERROR,
//                        "type=" + type.getName() + "获取 " + fileName + " 扩展失败");
//            }
//            while (urls.hasMoreElements()) {
//                URL url = urls.nextElement();
//                try (BufferedReader handler = new BufferedReader(
//                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
//                    String line;
//                    while ((line = handler.readLine()) != null) {
//                        line = line.trim();
//                        int index = line.indexOf("=");
//                        if (index < 1) {
//                            throw new MonitorException(CommonErrorCode.PLUGIN_ERROR,
//                                    "type=" + type.getName() + ",扩展配置格式错误 url=" + url.toString());
//                        }
//
//                        String key = StringUtil.assertTrimNotBlank(line.substring(0, index));
//
//                        if (key.equals(name)) {
//                            line = StringUtil.assertTrimNotBlank(line.substring(index + 1));
//                            if (line.length() > 0) {
//                                Class<?> clazz = Class.forName(line, false, Thread.currentThread().getContextClassLoader());
//                                if (!type.isAssignableFrom(clazz)) {
//                                    throw MonitorException.asMonitorException(CommonErrorCode.PLUGIN_ERROR,
//                                            "type=" + type.getName() + ", class: [" + line
//                                                    + "] is not subtype of "
//                                                    + type.getName());
//                                }
//                                @SuppressWarnings("unchecked")
//                                Class<? extends T> t = (Class<? extends T>) clazz;
//                                return t;
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new MonitorException(CommonErrorCode.PLUGIN_ERROR,
//                    "type=" + type.getName() + "获取 " + fileName + " 扩展失败");
//        }
//        return null;
//    }

    public T getPlugin(String name) {
        String fileName = SERVICE_DIR + type.getName();
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(fileName);
            if (null == urls) {
                throw MonitorException.asMonitorException(CommonErrorCode.PLUGIN_ERROR,
                        "type=" + type.getName() + "获取 " + fileName + " 扩展失败");
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();

                // jdk7 auto release resource
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        int index = line.indexOf("=");
                        if (index < 1) {
                            throw MonitorException.asMonitorException(CommonErrorCode.PLUGIN_ERROR,
                                    "type=" + type.getName() + ",扩展配置格式错误 url=" + url.toString());
                        }

                        String key = StringUtil.assertTrimNotBlank(line.substring(0, index));

                        if (key.equals(name)) {
                            line = StringUtil.assertTrimNotBlank(line.substring(index + 1));
                            if (line.length() > 0) {
                                Class<?> clazz = Class.forName(line, true, Thread.currentThread().getContextClassLoader());
                                if (!type.isAssignableFrom(clazz)) {
                                    throw MonitorException.asMonitorException(CommonErrorCode.PLUGIN_ERROR,
                                            "type=" + type.getName() + ", class: [" + line
                                                    + "] is not subtype of "
                                                    + type.getName());
                                }
                                @SuppressWarnings("unchecked")
                                T t = (T) clazz.newInstance();
                                return t;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new MonitorException(CommonErrorCode.PLUGIN_ERROR,
                    "type=" + type.getName() + "获取 " + fileName + " 扩展失败");
        }
        return null;
    }

}
