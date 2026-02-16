package co.ravn.ecommerce.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RestCallLogAspect {
    @Around("within(co.ravn.ecommerce.Controllers..*)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long initTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - initTime;
        System.out.println("============================================================================================================");
        System.out.println("Method Signature is : " + joinPoint.getSignature());
        System.out.println("Method executed in : " + executionTime + "ms");
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            System.out.println("Input Request: " + args[0]);
        }
        System.out.println("Output Response : " + proceed);
        return proceed;
    }
}
