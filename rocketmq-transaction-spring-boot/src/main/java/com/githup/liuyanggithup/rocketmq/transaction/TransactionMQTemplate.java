package com.githup.liuyanggithup.rocketmq.transaction;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * @author seventeen
 */
public class TransactionMQTemplate {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionMQTemplate.class);
    private RocketMQTemplate rocketMQTemplate;
    private TransactionMQRepository transactionMQRepository;
    private String gray;

    public TransactionMQRepository getTransactionMQRepository() {
        return transactionMQRepository;
    }

    public void setTransactionMQRepository(TransactionMQRepository transactionMQRepository) {
        this.transactionMQRepository = transactionMQRepository;
    }

    public String getGray() {
        return gray;
    }

    public void setGray(String gray) {
        this.gray = gray;
    }

    public RocketMQTemplate getRocketMQTemplate() {
        return rocketMQTemplate;
    }

    public void setRocketMQTemplate(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public TransactionMQSendResult sendTransaction(String topic, String tag, String msg) {
        return sendTransaction(topic, tag, null, msg);
    }

    public TransactionMQSendResult sendTransaction(String topic, String tag, String keys, String msg) {

        if (StrUtil.isBlank(keys)) {
            //自动设置keys
            keys = IdUtil.randomUUID();
        }
        TransactionMsgQueue transactionMsgQueue = new TransactionMsgQueue();
        transactionMsgQueue.setContent(msg);
        transactionMsgQueue.setTag(tag);
        transactionMsgQueue.setTopic(topic);
        transactionMsgQueue.setMsgKey(keys);
        transactionMsgQueue.setGray(gray);
        final Long tranId = transactionMQRepository.insertMsg(transactionMsgQueue);
        //事务提交后处理
        String finalKeys = keys;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                //处理该消息
                try {
                    LOGGER.info("提交事务消息成功，触发消息处理事件,id {}", tranId);
                    if (Objects.nonNull(tranId)) {
                        //方便扩展
                        Message message = new Message();
                        message.setTags(tag);
                        message.setTopic(topic);
                        message.setBody(msg.getBytes());
                        message.setKeys(finalKeys);
                        message.putUserProperty(TransactionMQConstant.GRAY, gray);
                        SendResult sendResult = rocketMQTemplate.getProducer().send(message);
                        if (sendResult.getSendStatus() == SendStatus.SEND_OK || sendResult.getSendStatus() == SendStatus.SLAVE_NOT_AVAILABLE) {
                            //删除消息
                            transactionMQRepository.deleteById(tranId);
                        } else {
                            LOGGER.error("处理提交事务消息失败，id {},sendResult {}", tranId, sendResult);
                        }
                    } else {
                        LOGGER.error("处理提交事务消息失败，id is null");
                    }
                } catch (Exception e) {
                    LOGGER.error("处理提交事务消息失败，id {}, exception ", tranId, e);
                }
            }
        });
        return TransactionMQSendResult.SEND_OK;
    }

}
