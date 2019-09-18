package com.sequoiadb.monitor.common.util;

import com.sequoiadb.monitor.common.constant.Constants;
import com.sequoiadb.monitor.common.util.sm2.DigitalUtil;
import com.sequoiadb.monitor.common.util.sm2.SM2Bean;
import com.sequoiadb.monitor.common.util.sm2.SM2Util;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

/**
 * @author xiejianhong@sequoiadb.com
 * @version 1.0
 * @date 2019/8/5 17:20
 */
public class EncryptUtil {

    private static final String SM2 = "SM2";

    public static String passwordEncrypt(String decryptType, String publicKey, String password) throws IllegalArgumentException {

        if (SM2.equals(decryptType)) {
            password = SM2Util.encrypt(DigitalUtil.hexToByte(publicKey), password.getBytes());
        } else {
            System.out.println("暂未实现" + decryptType + "类型的密码加密！");
        }
        return password;
    }

    public static String passwordDecrypt(String decryptType, String privateKey, String password) throws IllegalArgumentException {

        if (SM2.equals(decryptType)) {
            password = new String(SM2Util.decrypt(DigitalUtil.hexToByte(privateKey), DigitalUtil.hexToByte(password)));
        } else {
            System.out.println("暂未实现" + decryptType + "类型的密码解密！");
        }
        return password;
    }

    public static void generateEncryptPassword(String password) {
        SM2Bean sm2 = SM2Bean.Instance();
        AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ();

        String passwordPublicKey = DigitalUtil.byteToHex(publicKey.getEncoded(false));
        String passwordPrivateKey = DigitalUtil.byteToHex(privateKey.toByteArray());
        String encryptPassword = SM2Util.encrypt(DigitalUtil.hexToByte(passwordPublicKey), password.getBytes());

        System.out.println("公钥: " + passwordPublicKey);
        System.out.println("私钥: " + passwordPrivateKey);
        System.out.println("加密: " + encryptPassword);
    }
}
