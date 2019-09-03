package com.sequoiadb.monitor.common.util;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.util.sm2.DigitalUtil;
import com.sequoiadb.monitor.common.util.sm2.SM2Util;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/5 17:20
 */
public class EncryptUtil {

    private static final String PUBLIC_KEY = Configuration.getInstance().getStringProperty(Constants.MONITOR_SDB_PASSWORD_PUBLIC_KEY);
    private static final String PRIVATE_KEY = Configuration.getInstance().getStringProperty(Constants.MONITOR_SDB_PASSWORD_PRIVATE_KEY);
    private static final String DECRYPT_TYPE = Configuration.getInstance().getStringProperty(Constants.MONITOR_SDB_PASSWORD_ENCRYPT_TYPE);

    private static final String SM2 = "SM2";

    public static String passwordEncrypt(String password) throws IllegalArgumentException {

        if (SM2.equals(DECRYPT_TYPE)) {
            password = SM2Util.encrypt(DigitalUtil.hexToByte(PUBLIC_KEY), password.getBytes());
        } else {
            System.out.println("暂未实现" + DECRYPT_TYPE + "类型的密码加密！");
        }
        return password;
    }

    public static String passwordDecrypt(String password) throws IllegalArgumentException {
        if (SM2.equals(DECRYPT_TYPE)) {
            password = new String(SM2Util.decrypt(DigitalUtil.hexToByte(PRIVATE_KEY), DigitalUtil.hexToByte(password)));
        } else {
            System.out.println("暂未实现" + DECRYPT_TYPE + "类型的密码解密！");
        }
        return password;
    }
}
