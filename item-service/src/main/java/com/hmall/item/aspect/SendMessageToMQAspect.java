package com.hmall.item.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author axno
 * @date 2024/11/28 21:30
 * @DESCRIPTION
 */
@Aspect
@Component
@Slf4j
public class SendMessageToMQAspect {

    @Pointcut("execution(* )")
    public void SendMessageToMQPointCut(){}



}
