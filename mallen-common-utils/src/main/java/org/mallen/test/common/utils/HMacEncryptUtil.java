package org.mallen.test.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * hmac算法的工具类
 *
 * @author mallen
 * @date 2020/10/20
 */
public class HMacEncryptUtil {
    private static final Logger logger = LoggerFactory.getLogger(HMacEncryptUtil.class);

    public static String hmacSha1(String msg, String key) {
        return calcHex(msg, key, "HmacSHA1");
    }

    public static String hmacSha224(String msg, String key) {
        return calcHex(msg, key, "HmacSHA224");
    }

    public static String hmacSha256(String msg, String key) {
        return calcHex(msg, key, "HmacSHA256");
    }

    public static String hmacSha384(String msg, String key) {
        return calcHex(msg, key, "HmacSHA384");
    }

    public static String hmacSha512(String msg, String key) {
        return calcHex(msg, key, "HmacSHA512");
    }

    public static String hmacMd5(String msg, String key) {
        return calcHex(msg, key, "HmacMD5");
    }

    public static String calcBase64(String msg, String key, String algorithm) {
        byte[] result = doCalc(msg, key, algorithm);
        if (null == result) {
            return null;
        }
        return Base64.getEncoder().encodeToString(result);
    }

    public static String calcHex(String msg, String key, String algorithm) {
        byte[] result = doCalc(msg, key, algorithm);
        if (null == result) {
            return null;
        }
        // 转换为Hex
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private static byte[] doCalc(String msg, String key, String algorithm) {
        try {
            byte[] data = key.getBytes(Charset.forName("UTF-8"));
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKey secretKey = new SecretKeySpec(data, algorithm);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(algorithm);
            //用给定密钥初始化 Mac 对象
            mac.init(secretKey);

            byte[] text = msg.getBytes(Charset.forName("UTF-8"));
            //完成Mac操作
            return mac.doFinal(text);
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
            return null;
        } catch (InvalidKeyException e) {
            throw new RuntimeException("InvalidKey");
        }
    }
}
