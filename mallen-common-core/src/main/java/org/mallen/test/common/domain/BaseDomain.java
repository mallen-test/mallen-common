package org.mallen.test.common.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

/**
 * @author mallen
 * @date 20/3/19
 */
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class BaseDomain {
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

    @Override
    public String toString() {
        return "BaseDomain{" +
                "createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
