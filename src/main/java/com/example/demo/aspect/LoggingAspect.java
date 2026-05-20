package com.example.demo.aspect;

import com.example.demo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    @Pointcut("execution(* com.example.demo.services.impl.*.*(..))")
    public void serviceMethods() {}

    @Around("serviceMethods()")
    public Object profileServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        String userEmail = SecurityUtils.getCurrentUserEmail();

        boolean isWriteOperation = isWriteOperation(methodName);

        if (!isWriteOperation) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();

        log.info("[SERVICE START] {}.{} | User: {} | Args: {}", className, methodName, userEmail, Arrays.toString(args));

        Object result;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[SERVICE END] {}.{} | Status: SUCCESS | Duration: {}ms", className, methodName, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            if (isBusinessException(e)) {
                log.warn("[SERVICE END] {}.{} | Status: FAILED | Level: WARN | Error: {}", className, methodName, e.getMessage());
            } else {
                log.error("[SERVICE END] {}.{} | Status: FAILED | Level: ERROR | Error: {} | Duration: {}ms", className, methodName, e.getMessage(), duration, e);
            }
            
            throw e;
        }
    }

    private boolean isBusinessException(Exception e) {
        String className = e.getClass().getSimpleName();
        return className.contains("BadRequestException") || 
               className.contains("DataNotFoundException") || 
               className.contains("UnauthorizedException") || 
               className.contains("DuplicateResourceException") ||
               className.contains("AccountLockedException");
    }
private boolean isWriteOperation(String methodName) {
    String name = methodName.toLowerCase();
    return name.contains("create") || 
           name.contains("update") || 
           name.contains("delete") || 
           name.contains("save") || 
           name.contains("toggle") || 
           name.contains("activate") || 
           name.contains("deactivate") ||
           name.contains("handle") ||
           name.contains("logout") ||
           name.contains("change");
}
}
