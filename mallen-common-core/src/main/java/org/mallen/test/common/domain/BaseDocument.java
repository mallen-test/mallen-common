package org.mallen.test.common.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

/**
 * mongodb数据库的基类，所有mongodb的domain对象都需要继承于该类，该类要求设置创建时间和更新时间。
 * 通过调用spring-data-mongodb的方法可以自动完成创建时间和更新时间的设置，由于当前版本的spring-data-mongodb本身的问题，
 * 如果是bulk操作，不会更新这两个字段的值，请手动设置。
 * Created by mallen on 10/09/18.
 */
public class BaseDocument {
    @Id
    private String id;
    @CreatedDate
    private Date createdTime;
    @LastModifiedDate
    private Date updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "BaseDocument{" +
                "id='" + id + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
