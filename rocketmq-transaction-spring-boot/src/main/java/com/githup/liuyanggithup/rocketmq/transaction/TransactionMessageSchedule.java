package com.githup.liuyanggithup.rocketmq.transaction;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: seventeen
 */
public class TransactionMessageSchedule {
    private final static Logger LOGGER = LoggerFactory.getLogger(TransactionMessageSchedule.class);

    private TransactionMQTemplate transactionMQTemplate;

    public TransactionMQTemplate getTransactionMQTemplate() {
        return transactionMQTemplate;
    }

    public void setTransactionMQTemplate(TransactionMQTemplate transactionMQTemplate) {
        this.transactionMQTemplate = transactionMQTemplate;
    }

    @Scheduled(fixedRate = 60000)
    @SchedulerLock(
            name = "${rocketmq.transaction.gray:default}" + "-mqTransactionScheduledTask",
            lockAtMostFor = "2m",
            lockAtLeastFor = "10s"
    )
    public void scheduledTask() {

        try {
            String gray = transactionMQTemplate.getGray();
            TransactionMQRepository transactionMQRepository = transactionMQTemplate.getTransactionMQRepository();
            List<TransactionMsgQueue> list = transactionMQRepository.findFailMsg(gray);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            list.forEach(transactionMsgQueue -> {
                Long id = transactionMsgQueue.getId();
                String content = transactionMsgQueue.getContent();
                Message message = new Message();
                message.setTags(transactionMsgQueue.getTag());
                message.setTopic(transactionMsgQueue.getTopic());
                message.setBody(content.getBytes());
                message.setKeys(transactionMsgQueue.getMsgKey());
                message.putUserProperty(TransactionMQConstant.GRAY, gray);
                RocketMQTemplate rocketMQTemplate = transactionMQTemplate.getRocketMQTemplate();
                TransactionMQSendResult transactionMQSendResult;
                try {
                    SendResult sendResult = rocketMQTemplate.getProducer().send(message);
                    if (sendResult.getSendStatus() == SendStatus.SEND_OK || sendResult.getSendStatus() == SendStatus.SLAVE_NOT_AVAILABLE) {
                        //删除消息
                        transactionMQSendResult = TransactionMQSendResult.SEND_OK;
                        LOGGER.info("重试发送事务消息成功 id{}", id);
                    } else {
                        transactionMQSendResult = TransactionMQSendResult.SEND_ERROR;
                        LOGGER.info("重试发送事务消息失败 id{},result {}", id, sendResult.toString());
                    }
                } catch (Exception e) {
                    transactionMQSendResult = TransactionMQSendResult.SEND_ERROR;
                    LOGGER.info("重试发送事务消息失败 id{}, exception:", id, e);
                }

                if (transactionMQSendResult == TransactionMQSendResult.SEND_OK) {
                    transactionMQRepository.deleteById(id);
                } else {
                    transactionMQRepository.addRetryNum(id);
                }
            });
        } catch (Throwable throwable) {
            LOGGER.info("事务消息重试任务执行失败,", throwable);
        } finally {
            LOGGER.info("事务消息重试任务执行");
        }

    }


}
