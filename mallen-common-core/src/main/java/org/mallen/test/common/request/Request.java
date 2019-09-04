package org.mallen.test.common.request;

/**
 * @author mallen
 * @date 8/30/19
 */
public class Request<T> {
    private Long timestamp;
    private T data;

    public Request() {
    }

    public Request(T data) {
        this.timestamp = System.currentTimeMillis();
        this.data = data;
    }

    public Request(Long timestamp, T data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public static <T> Request create(T data) {
        return new Request(data);
    }

    public static <T> Request create(T data, Long timestamp) {
        return new Request(timestamp, data);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
