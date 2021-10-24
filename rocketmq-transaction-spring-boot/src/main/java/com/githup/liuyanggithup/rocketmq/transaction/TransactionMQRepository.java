package com.githup.liuyanggithup.rocketmq.transaction;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author seventeen
 */
public class TransactionMQRepository {

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insertMsg(final TransactionMsgQueue transactionMsgQueue) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(TransactionMQConstant.INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, transactionMsgQueue.getTopic());
            ps.setString(2, transactionMsgQueue.getTag());
            ps.setString(3, transactionMsgQueue.getMsgKey());
            ps.setString(4, transactionMsgQueue.getContent());
            ps.setString(5, transactionMsgQueue.getGray());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void deleteById(Long id) {
        jdbcTemplate.update(TransactionMQConstant.DELETE_BY_ID_SQL, 1, id);
    }


    public void addRetryNum(Long id) {
        jdbcTemplate.update(TransactionMQConstant.ADD_RETRY_NUM_SQL, id);
    }

    public List<TransactionMsgQueue> findFailMsg(String gray) {

        DateTime dateTime = DateUtil.offsetMinute(new Date(), -5);
        String createAtBefore = DateUtil.format(dateTime, TransactionMQConstant.TIME_FORMAT);
        //sql语句
        List<TransactionMsgQueue> list = jdbcTemplate.query(conn -> {
            PreparedStatement ps = conn.prepareStatement(TransactionMQConstant.FIND_FAIL_LIST_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, gray);
            ps.setString(2, createAtBefore);
            return ps;
        }, (rs, i) -> {
            //从结果集里把数据得到
            Long id = rs.getLong("id");
            String topic = rs.getString("topic");
            String tag = rs.getString("tag");
            String content = rs.getString("content");
            String msgKey = rs.getString("msg_key");
            String gray1 = rs.getString("gray");
            Date createdAt = new Date(rs.getTimestamp("created_at").getTime());
            Date updatedAt = new Date(rs.getTimestamp("updated_at").getTime());
            Boolean deleted = rs.getBoolean("deleted");
            //把数据封装到对象里
            TransactionMsgQueue msgQueue = new TransactionMsgQueue();
            msgQueue.setId(id);
            msgQueue.setTopic(topic);
            msgQueue.setTag(tag);
            msgQueue.setContent(content);
            msgQueue.setMsgKey(msgKey);
            msgQueue.setGray(gray1);
            msgQueue.setCreatedAt(createdAt);
            msgQueue.setUpdatedAt(updatedAt);
            msgQueue.setDeleted(deleted);
            return msgQueue;
        });
        return list;
    }

}
