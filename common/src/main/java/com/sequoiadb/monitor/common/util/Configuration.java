package com.sequoiadb.monitor.common.util;

import com.sequoiadb.monitor.common.exception.CommonErrorCode;
import com.sequoiadb.monitor.common.exception.MonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/7/31 8:26
 */
public class Configuration {

    private final Logger log = LoggerFactory.getLogger(Configuration.class);

    private Map<String, Object> props = new ConcurrentHashMap<>();


    private Configuration() {
    }

    private static class InstanceHolder {
        private final static Configuration INSTANCE = new Configuration();
    }

    public static Configuration getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void parse(String configFileName) {

        log.info("begin to parse config file.");
        Properties prop = new Properties();
        File configFile = new File(configFileName);
        if (configFile.exists()) {
            try {
                prop.load(new FileInputStream(configFile));
            } catch (IOException e) {
                log.error("failed to parse config file.", e);
                throw MonitorException.asMonitorException(CommonErrorCode.CONFIG_ERROR, "failed to parse config file.", e);
            }
        } else {
            log.error("config file [{}] is not exist.", configFile.getAbsoluteFile());
            throw MonitorException.asMonitorException(CommonErrorCode.CONFIG_ERROR,
                    "config file[" + configFile.getAbsolutePath() + "] is not exits.");
        }
        for(String key : prop.stringPropertyNames()) {
            props.put(key, prop.getProperty(key));
        }
        log.info("finish to parse config file.");
    }

    public void putObjectProperty(String key, Object object) {
        props.put(key, object);
    }

    public Object getObjectProperty(String key) {
        return props.get(key);
    }

    public boolean getBooleanFromStringProperty(String item) {

        String value = getStringProperty(item, "off");
        return "on".equalsIgnoreCase(value);
    }

    public String getStringProperty(String name) {
        return getStringProperty(name, null);
    }


    public String getStringProperty(String name, String def) {
        Object val = props.get(name);
        if (val == null) {
            return def;
        }

        String strVal = ((String)val).trim();

        return (strVal.length() == 0) ? def : strVal;
    }

    public String[] getStringArrayProperty(String name) {
        return getStringArrayProperty(name, null);
    }

    public String[] getStringArrayProperty(String name, String[] def) {
        String vals = getStringProperty(name);
        if (vals == null) {
            return def  ;
        }

        StringTokenizer stok = new StringTokenizer(vals, ",");
        ArrayList<String> strs = new ArrayList<String>();
        try {
            while (stok.hasMoreTokens()) {
                strs.add(stok.nextToken().trim());
            }

            return strs.toArray(new String[0]);
        } catch (Exception e) {
            return def;
        }
    }

    public List<String> getStringListProperty(String name) {
        return getStringListProperty(name, new ArrayList());
    }

    public List<String> getStringListProperty(String name, List def) {
        String vals = getStringProperty(name);
        if (vals == null) {
            return def  ;
        }

        StringTokenizer stok = new StringTokenizer(vals, ",");
        ArrayList<String> strs = new ArrayList<String>();
        try {
            while (stok.hasMoreTokens()) {
                strs.add(stok.nextToken().trim());
            }
            return strs;
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    public boolean getBooleanProperty(String name, boolean def) {
        String val = getStringProperty(name);

        return (val == null) ? def : Boolean.valueOf(val).booleanValue();
    }

    public byte getByteProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Byte.parseByte(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public byte getByteProperty(String name, byte def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Byte.parseByte(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public char getCharProperty(String name) {
        return getCharProperty(name, '\0');
    }

    public char getCharProperty(String name, char def) {
        String param = getStringProperty(name);
        return  (param == null) ? def : param.charAt(0);
    }

    public double getDoubleProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public double getDoubleProperty(String name, double def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public float getFloatProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public float getFloatProperty(String name, float def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public int getIntProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public int getIntProperty(String name, int def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public int[] getIntArrayProperty(String name) throws NumberFormatException {
        return getIntArrayProperty(name, null);
    }

    public int[] getIntArrayProperty(String name, int[] def)
            throws NumberFormatException {
        String vals = getStringProperty(name);
        if (vals == null) {
            return def;
        }

        StringTokenizer stok = new StringTokenizer(vals, ",");
        ArrayList<Integer> ints = new ArrayList<Integer>();
        try {
            while (stok.hasMoreTokens()) {
                try {
                    ints.add(new Integer(stok.nextToken().trim()));
                } catch (NumberFormatException nfe) {
                    throw new NumberFormatException(" '" + vals + "'");
                }
            }

            int[] outInts = new int[ints.size()];
            for (int i = 0; i < ints.size(); i++) {
                outInts[i] = ((Integer)ints.get(i)).intValue();
            }
            return outInts;
        } catch (Exception e) {
            return def;
        }
    }

    public long getLongProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public long getLongProperty(String name, long def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public short getShortProperty(String name) throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            throw new NumberFormatException(" null string");
        }

        try {
            return Short.parseShort(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public short getShortProperty(String name, short def)
            throws NumberFormatException {
        String val = getStringProperty(name);
        if (val == null) {
            return def;
        }

        try {
            return Short.parseShort(val);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException(" '" + val + "'");
        }
    }

    public String[] getPropertyGroups(String prefix) {
        Iterator<String> keyIterator = props.keySet().iterator();
        HashSet<String> groups = new HashSet<String>(10);

        if (!prefix.endsWith(".")) {
            prefix += ".";
        }

        while (keyIterator.hasNext()) {
            String key = (String) keyIterator.next();
            if (key.startsWith(prefix)) {
                groups.add(key);
            }
        }

        return groups.toArray(new String[groups.size()]);
    }

    public Properties getPropertyGroup(String prefix) {
        return getPropertyGroup(prefix, false, null);
    }

    public Properties getPropertyGroup(String prefix, boolean stripPrefix) {
        return getPropertyGroup(prefix, stripPrefix, null);
    }

    /**
     * Get all properties that start with the given prefix.
     *
     **/
    public Properties getPropertyGroup(String prefix, boolean stripPrefix, String[] excludedPrefixes) {
        Iterator<String> keyIterator = props.keySet().iterator();
        Properties group = new Properties();

        if (!prefix.endsWith(".")) {
            prefix += ".";
        }

        while (keyIterator.hasNext()) {
            String key = (String)keyIterator.next();
            if (key.startsWith(prefix)) {

                boolean exclude = false;
                if (excludedPrefixes != null) {
                    for (int i = 0; (i < excludedPrefixes.length) && (exclude == false); i++) {
                        exclude = key.startsWith(excludedPrefixes[i]);
                    }
                }

                if (!exclude) {
                    String value = getStringProperty(key, "");

                    if (stripPrefix) {
                        group.put(key.substring(prefix.length()), value);
                    } else {
                        group.put(key, value);
                    }
                }
            }
        }

        return group;
    }
}
