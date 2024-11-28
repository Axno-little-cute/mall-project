package com.hmall.item.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author axno
 * @date 2024/11/28 21:27
 * @DESCRIPTION
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SendMessageToMQ {
    String exchange();  //交换机
    String routingKey();  //路由键
}
