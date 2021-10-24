package com.githup.liuyanggithup.rocketmq.transaction;

/**
 * @author seventeen
 */
public enum TransactionMQSendResult {
    /**
     * 发送成功
     */
    SEND_OK,
    /**
     * 发送失败
     */
    SEND_ERROR;

    private TransactionMQSendResult() {
    }
}
