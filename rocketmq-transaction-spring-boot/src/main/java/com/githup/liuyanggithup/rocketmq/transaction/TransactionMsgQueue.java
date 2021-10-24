package com.githup.liuyanggithup.rocketmq.transaction;

import java.util.Date;

/**
 * @author seventeen
 */
public class TransactionMsgQueue {

    private Long id;

    private String topic;

    private String tag;

    private String msgKey;

    private String content;

    /**
     * 灰度标记，数据库层面支持灵活灰度
     */
    private String gray;

    /**
     * 数据创建时间
     */
    private Date createdAt;

    /**
     * 数据修改时间
     */
    private Date updatedAt;

    /**
     * 是否删除 0(false):未删除,1(true):已删除
     */
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGray() {
        return gray;
    }

    public void setGray(String gray) {
        this.gray = gray;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
