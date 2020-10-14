package org.mallen.test.common.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 消息摘要算法
 *
 * @author mallen
 * @date 2020/10/14
 */
public class MessageDigestUtil {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(MessageDigestUtil.md5("123123123"));
    }

    public static String md5(String msg) {
        return calc(msg, "MD5");
    }

    public static String sha1(String msg) {
        return calc(msg, "SHA-1");
    }

    public static String sha224(String msg) {
        return calc(msg, "SHA-224");
    }

    public static String sha256(String msg) {
        return calc(msg, "SHA-256");
    }

    public static String sha384(String msg) {
        return calc(msg, "SHA-384");
    }

    public static String sha512(String msg) {
        return calc(msg, "SHA-512");
    }

    private static String calc(String msg, String algorithm) {
        try {
            return doCalc(msg.getBytes(Charset.forName("UTF-8")), algorithm);
        } catch (NoSuchAlgorithmException e) {

        }

        return null;
    }

    /**
     * 计算摘要，algorithm的可取值参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest
     *
     * @param input
     * @param algorithm 摘要算法，可取值：
     *                  MD2
     *                  MD5
     *                  SHA-1
     *                  SHA-224
     *                  SHA-256
     *                  SHA-384
     *                  SHA-512
     *                  SHA-512/224
     *                  SHA-512/256
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String doCalc(byte[] input, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance(algorithm);
        byte[] result = mDigest.digest(input);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
