package com.meb.account_management.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info("{} executed in {}ms", joinPoint.getSignature(), executionTime);
        return proceed;
    }

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

//    @Pointcut("@within(org.springframework.stereotype.Service)")
    @Pointcut("execution(* com.meb.account_management.service.*.*(..))")
    public void allServiceMethods() {}

    @Before("allServiceMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        logger.info("[Enter] {}.{}() | Args: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                getArgs(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "allServiceMethods()", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        logger.info("[Return] {}.{}() | Result: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result);
    }

    @AfterThrowing(pointcut = "allServiceMethods()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        logger.error("[Exception] {}.{}() | Exception: {} | Args: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.toString(),
                getArgs(joinPoint.getArgs()));
    }

    private String getArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "no args";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append("arg[").append(i).append("]=");
            if (args[i] != null) {
                sb.append(args[i].toString());
            } else {
                sb.append("null");
            }
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}