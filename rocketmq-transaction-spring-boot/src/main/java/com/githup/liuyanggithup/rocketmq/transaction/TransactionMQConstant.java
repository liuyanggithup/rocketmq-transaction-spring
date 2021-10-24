package com.githup.liuyanggithup.rocketmq.transaction;

/**
 * @author seventeen
 */
public class TransactionMQConstant {

    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_GRAY = "default";

    public static final String GRAY = "gray";
    
    public static final String DEFAULT_TRANSACTION_MQ_DATASOURCE_NAME = "datasource";

    public static final String INSERT_SQL = "insert into tran_mq_queue (topic,tag,msg_key,content,gray,retry_num) values(?,?,?,?,?,0)";

    public static final String DELETE_BY_ID_SQL = "UPDATE tran_mq_queue SET deleted = ? WHERE id = ?";

    public static final String FIND_FAIL_LIST_SQL = "select * from tran_mq_queue where deleted = 0 and gray = ? and created_at<? order by id desc limit 1000";

    public static final String ADD_RETRY_NUM_SQL = "UPDATE tran_mq_queue SET retry_num = retry_num+1 WHERE id = ?";
}
