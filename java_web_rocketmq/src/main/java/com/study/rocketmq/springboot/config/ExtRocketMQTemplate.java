package com.study.rocketmq.springboot.config;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

/**
 * 自定义定制template！
 * @author ：楼兰
 * @date ：Created in 2020/12/4
 * @description:
 **/
@ExtRocketMQTemplateConfiguration()
public class ExtRocketMQTemplate extends RocketMQTemplate {
}
