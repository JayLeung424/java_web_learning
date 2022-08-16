package com.study.rocketmq.springboot.basic;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 注意下@RocketMQMessageListener这个注解的其他属性
 * @author ：楼兰
 * @date ：Created in 2020/10/22
 * @description:
 **/
@Component
@RocketMQMessageListener(
        consumerGroup = "MyConsumerGroup",
        topic = "TestTopic",
        consumeMode= ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING,
        selectorType = SelectorType.TAG,
        selectorExpression = "*"
)
public class SpringConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        System.out.println("Received message : "+ message);
    }
}
