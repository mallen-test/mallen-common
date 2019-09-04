package org.mallen.test.common.response;

import java.util.List;

/**
 * @author mallen
 * @date 3/20/19
 */
public class SlicePage<T> {
    private List<T> data;
    private Boolean hasNext;

    public SlicePage() {
    }

    public SlicePage(List<T> data, Boolean hasNext) {
        this.data = data;
        this.hasNext = hasNext;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
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
