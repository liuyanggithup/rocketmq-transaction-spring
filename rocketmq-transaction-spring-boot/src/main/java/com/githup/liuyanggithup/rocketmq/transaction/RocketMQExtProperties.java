package com.githup.liuyanggithup.rocketmq.transaction;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author seventeen
 */
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMQExtProperties {

    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public static class Transaction {
        private String datasource;

        private String gray = TransactionMQConstant.DEFAULT_GRAY;

        public String getDatasource() {
            return datasource;
        }

        public void setDatasource(String datasource) {
            this.datasource = datasource;
        }

        public String getGray() {
            return gray;
        }

        public void setGray(String gray) {
            this.gray = gray;
        }
    }
}
