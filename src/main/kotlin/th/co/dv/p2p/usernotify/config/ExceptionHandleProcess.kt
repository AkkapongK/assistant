package th.co.dv.p2p.usernotify.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import th.co.dv.p2p.common.base.services.RedisHandlerProcess
import th.co.dv.p2p.common.enums.Services

/**
 * Class that manage lock record process
 * we call normal method that used annotation TransactionalExceptionHandler from `joinPoint.proceed()`
 * after that normal method finished process
 * we automatic deleted record in Redis by the key that used for lock
 */
@Aspect
@Component
// TODO: Change service
class ExceptionHandleProcess: RedisHandlerProcess(Services.CREDIT_NOTE) {

    @Around("@annotation(th.co.dv.p2p.common.base.annotations.TransactionExceptionHandler)")
    fun exceptionHandling(joinPoint: ProceedingJoinPoint) = process(joinPoint)
}