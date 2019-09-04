package org.mallen.test.common.response;


import org.mallen.test.common.exception.IError;

import java.io.Serializable;

/**
 * @author mallen
 * @date 20/3/19
 * @param <T>
 */
public class Response<T> implements Serializable {
    private Status status = Status.SUCCEED;
    private String errorCode;
    private String errorMessage;
    private String extMessage;
    private Long total;
    /**
     * 以下4个字段：totalCount，pageIndex，pageSize，pageCount建议应用不再使用。
     * 使用total替换totalCount，另外三个字段建议不要返回。
     * pageIndex，pageSize，pageCount在客户端可以直接获取到不需要服务器段再进行计算并返回。此处列出仅为兼容老版本接口
     */
    private Long totalCount;
    /**
     * 是否还有下一页信息，适用于瀑布流式分页
     */
    private Boolean hasNext;
    private T data;

    public Response() {

    }


    public Response(IError error) {
        this.errorCode = error.getErrorCode();
        this.errorMessage = error.getErrorMessage();
        this.status = Status.FAILED;
    }

    public Response(Long total) {
        this.total = total;
    }

    public Response(T data) {
        this.data = data;
    }

    public Response(T data, Long total) {
        this.data = data;
        this.total = total;
    }

    public Response(T data, Boolean hasNext) {
        this.data = data;
        this.hasNext = hasNext;
    }

    public Response(Long total, T data) {
        this.data = data;
        this.total = total;
    }

    public static Response create() {
        return new Response();
    }

    public static Response create(IError error) {
        Response response = new Response();
        response.errorCode = error.getErrorCode();
        response.errorMessage = error.getErrorMessage();
        response.status = Status.FAILED;
        return response;
    }

    public static <T> Response create(T data) {
        return new Response(data);
    }

    public static Response create(Long total) {
        return new Response(total);
    }

    public static <T> Response create(T data, Long total) {
        return new Response(data, total);
    }

    public static <T> Response create(Long total, T data) {
        return new Response(data, total);
    }

    public static <T> Response create(T data, Boolean hasNext) { return new Response(data, hasNext);}

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExtMessage() {
        return extMessage;
    }

    public void setExtMessage(String extMessage) {
        this.extMessage = extMessage;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", extMessage='" + extMessage + '\'' +
                ", total=" + total +
                ", totalCount=" + totalCount +
                ", hasNext=" + hasNext +
                ", data=" + data +
                '}';
    }

    public enum Status {
        SUCCEED, FAILED
    }
}
