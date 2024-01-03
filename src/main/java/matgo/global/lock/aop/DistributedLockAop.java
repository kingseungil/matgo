package matgo.global.lock.aop;


import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.lock.annotation.DistributedLock;
import matgo.global.lock.util.CustomSpringELParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(matgo.global.lock.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(),
          joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key); // 1. 락 이름으로 RLOCK 인스턴스 생성

        try {
            log.info("try lock : {}", key);
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
              distributedLock.timeUnit()); // 2. 락 획득 시도
            if (!available) {
                return false;
            }
            return aopForTransaction.proceed(joinPoint); // 3. 트랜잭션 분리
        } catch (InterruptedException e) {
            log.error("Redisson Lock Interrupted in {} / {}", method.getName(), key);
            throw new InterruptedException("락 획득 실패");
        } finally {
            try {
                log.info("unlock : {}", key);
                rLock.unlock(); // 4. 락 해제
            } catch (IllegalMonitorStateException e) {
                log.error("Redisson Lock Already UnLock in {} / {}", method.getName(), key);
            }
        }
    }
}
