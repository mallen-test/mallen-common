package org.mallen.test.common.response;

/**
 * @author mallen
 * @date 3/20/19
 */
public class SlicePage<T> {
    private T data;
    private Boolean hasNext;

    public SlicePage() {
    }

    public SlicePage(T data, Boolean hasNext) {
        this.data = data;
        this.hasNext = hasNext;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    @Override
    public String toString() {
        return "SlicePage{" +
                "data=" + data +
                ", hasNext=" + hasNext +
                '}';
    }
}
