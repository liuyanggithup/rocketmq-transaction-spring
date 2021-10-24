# RocketMQ Transaction Spring

#### 介绍
RocketMQ Transaction Spring 是官方框架 RocketMQ Spring的扩展，提供了应用数据库事务消息表实现消息最终一致性的扩展方案。


#### 项目起因
Apache RocketMQ 4.3 版本开始支持事务消息，但已知4.7之前版本存在BUG（参考：https://github.com/apache/rocketmq/issues/1183）。
作者也亲身验证4.4的版本因OP队列文件丢失导致大量的历史事务消息回查的问题，导致生产故障。
因此产生了提供一种简单使用透明地解决方案的想法并于2021年10月24日开发完成，谨以此框架纪念2021年的1024。

#### 软件架构
> 软件架构说明

关于使用数据库表实现可靠消息发送一致性的问题，可以参考
- [去哪儿网QMQ的事务消息文档] https://github.com/qunarcorp/qmq/blob/master/docs/cn/transaction.md

众所周知，实现可靠消息最终一致性，要考虑两个方面发送端保证消息的发送成功和本地事务的一致性，消费端保证最终消费成功。
消费端RocketMQ提供了重试机制来保证，我们只考虑消息发送和事务提交的最终一致性。

主要方案如下：

1.  事务消息记录表和本地事务在一个事务中一起提交或回滚
2.  事务提交成功，框架开启处理该条消息，如果发送成功，则标记事务消息记录表记录为已发送，失败则忽略
3.  框架内置补偿任务，每分钟查询一分钟前的1000条未发送成功地消息做重试，并根据状态修改事务消息记录表

一切都是框架自动完成，开发简单方便。

#### 安装教程

创建表

```
#事务消息表，代码没有校验字段长度，可以自己调整SQL
CREATE TABLE `tran_mq_queue` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `topic` varchar(256) NOT NULL DEFAULT '',
  `tag` varchar(256) NOT NULL DEFAULT '',
  `msg_key` varchar(256) NOT NULL DEFAULT '',
  `content` text NOT NULL,
  `gray` varchar(256) NOT NULL DEFAULT '',
  `retry_num` int(11) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_gray_deleted_created_at` (`gray`(32),`deleted`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;

#事务消息补偿任务数据库锁，保证每个灰度环境只有一个实例任务会执行
CREATE TABLE `tran_mq_lock` (
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `lock_until` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `locked_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `locked_by` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
导入依赖

> 下载对应的version版本,目前是测试版0.1
```
<dependency>
   <groupId>com.githup.liuyanggithup</groupId>
   <artifactId>rocketmq-transaction-spring-boot-starter</artifactId>
   <version>${version}</version>
</dependency>
```

配置

```
#配置name-server，与官方包一致
rocketmq.name-server = 127.0.0.1:9876
#配置生产组名，与官方包一致
rocketmq.producer.group = test-producer
#配置事务消息表所用的数据源，如果不配置则默认从容器中取datasource
rocketmq.transaction.datasource = xxxDatasource
#配置事务消息表灰度环境，如果不配置则默认灰度值default，若多个应用使用同一个库的事务消息表则应配置不同值做区分
rocketmq.transaction.gray = xxx
```


代码


```
    @Resource
    private TransactionMQTemplate transactionMQTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void demo() {
        //本地事务代码
        //省略
        
        //发送事务消息
        transactionMQTemplate.sendTransaction("test","tagA","dddfdsfafa");
    }
```

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

例如：你可以在samples包下新建样例工程，编写样例代码测试，提交PR参与贡献

#### 交流

> QQ群：721567149 加入QQ群后添加作者微信申请加入微信群


