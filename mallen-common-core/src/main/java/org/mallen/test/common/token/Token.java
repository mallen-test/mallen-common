package org.mallen.test.common.token;

/**
 * 代表token的对象
 *
 * @author mallen
 * @date 2020/10/22
 */
public class Token {
    private String aid;
    /**
     * 请求url中的appid参数，可能为空
     */
    private String appId;
    /**
     * token的过期时间点，超过该时间表示token已过期。毫秒级别时间戳
     */
    private Long expireAt;

    @Override
    public String toString() {
        return "Token{" +
                "aid='" + aid + '\'' +
                ", expireAt=" + expireAt +
                ", appId='" + appId + '\'' +
                '}';
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
