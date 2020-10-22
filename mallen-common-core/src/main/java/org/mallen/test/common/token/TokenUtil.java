package org.mallen.test.common.token;

/**
 * 微服务环境中，用于从header中获取token，并保存到本地变量中的
 * @author mallen
 * @date 2020/10/22
 */
public class TokenUtil {
    private static ThreadLocal<Token> localToken = new ThreadLocal<>();

    public static void setToken(Token token) {
        localToken.set(token);
    }

    public static Token getToken() {
        return localToken.get();
    }

    public static void removeToken() {
        localToken.remove();
    }

    public static String getAid() {
        Token token = getToken();
        if (token == null) {
            throw new RuntimeException("token is not exist");
        }

        return token.getAid();
    }
}
