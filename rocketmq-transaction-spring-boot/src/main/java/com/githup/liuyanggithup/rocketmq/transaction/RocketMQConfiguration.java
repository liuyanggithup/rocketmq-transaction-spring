package com.githup.liuyanggithup.rocketmq.transaction;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author seventeen
 */
@Configuration
@Component
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableConfigurationProperties(RocketMQExtProperties.class)
public class RocketMQConfiguration implements ApplicationContextAware {

    private static final String PRODUCER_BEAN_NAME = "rocketMQTemplate";
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    public JdbcTemplate rocketMQJdbcTemplate(RocketMQExtProperties rocketMQExtProperties) {

        DataSource dataSource;
        String transactionDatasource = rocketMQExtProperties.getTransaction().getDatasource();
        if (StringUtils.isBlank(transactionDatasource)) {
            dataSource = applicationContext.getBean(TransactionMQConstant.DEFAULT_TRANSACTION_MQ_DATASOURCE_NAME, DataSource.class);
        } else {
            dataSource = applicationContext.getBean(transactionDatasource, DataSource.class);
        }
        return new JdbcTemplate(dataSource);
    }

    /**
     * CREATE TABLE tran_mq_lock(name VARCHAR(64) NOT NULL, lock_until TIMESTAMP(3) NOT NULL,
     * locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), locked_by VARCHAR(255) NOT NULL, PRIMARY KEY (name));
     *
     * @param rocketMQJdbcTemplate jdbc
     * @return LockProvider
     */
    @Bean
    public LockProvider lockProvider(JdbcTemplate rocketMQJdbcTemplate) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(rocketMQJdbcTemplate)
                        .withTableName("tran_mq_lock")
                        .usingDbTime()
                        .build()
        );
    }

    @Bean
    public TransactionMQRepository transactionMQRepository(JdbcTemplate rocketMQJdbcTemplate) {
        TransactionMQRepository transactionMQRepository = new TransactionMQRepository();
        transactionMQRepository.setJdbcTemplate(rocketMQJdbcTemplate);
        return transactionMQRepository;
    }

    @Bean
    public TransactionMQTemplate transactionMQTemplate(TransactionMQRepository transactionMQRepository,RocketMQExtProperties rocketMQExtProperties) {
        TransactionMQTemplate transactionMQTemplate = new TransactionMQTemplate();
        if (applicationContext.containsBean(PRODUCER_BEAN_NAME)) {
            transactionMQTemplate.setRocketMQTemplate(applicationContext.getBean(PRODUCER_BEAN_NAME, RocketMQTemplate.class));
        }
        transactionMQTemplate.setTransactionMQRepository(transactionMQRepository);
        String gray = rocketMQExtProperties.getTransaction().getGray();
        transactionMQTemplate.setGray(gray);
        return transactionMQTemplate;
    }

    @Bean
    public TransactionMessageSchedule TransactionMessageSchedule(TransactionMQTemplate transactionMQTemplate) {
        TransactionMessageSchedule transactionMessageSchedule = new TransactionMessageSchedule();
        transactionMessageSchedule.setTransactionMQTemplate(transactionMQTemplate);
        return transactionMessageSchedule;
    }
}
