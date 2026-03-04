package co.ravn.ecommerce.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class RestCallLogAspect {
    @Around("within(co.ravn.ecommerce.Controllers..*)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long initTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - initTime;
        log.info("============================================================================================================");
        log.info("Method Signature is : " + joinPoint.getSignature());
        log.info("Method executed in : " + executionTime + "ms");
        Object[] args = joinPoint.getArgs();
        log.info("Input Request: " + Arrays.toString(args));
        log.info("Output Response : " + proceed);
        return proceed;
    }
}
