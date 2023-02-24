package th.co.dv.p2p.common.base.services

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import th.co.dv.p2p.common.base.utilities.deleteRecord
import th.co.dv.p2p.common.constants.REQUIRED_TRANSACTION_ID_IN_ARG
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.utilities.SponsorContextHolder

abstract class RedisHandlerProcess(val service: Services) {

    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>

    /**
     * Method for handle lock record in flow that no need to wait other services
     */
    protected fun process(joinPoint: ProceedingJoinPoint): Any? {
        return try {
            val result = joinPoint.proceed()
            result
        } catch (e: Exception) {
            val methodSignature = joinPoint.signature as MethodSignature

            val parameterNames = methodSignature.parameterNames.toList()

            val parameterValues = joinPoint.args.toList()

            // Get transaction id
            val transactionId = getTransactionId(parameterNames, parameterValues)

            // delete lock
            redisTemplate.deleteRecord(
                    transactionId = transactionId,
                    sponsor = SponsorContextHolder.getCurrentSponsor()!!,
                    service = service)

            throw e
        }
    }

    /**
     * Method for get transaction id from parameter that pass in process
     *
     * @param parameterNames: parameter name in method (must have name transactionId)
     * @param parameterValues: parameter value in method
     */
    protected fun getTransactionId(parameterNames: List<String>, parameterValues: List<Any>): String {
        val transactionIdIndex = parameterNames.indexOf("transactionId")

        if (transactionIdIndex == -1) throw IllegalArgumentException(REQUIRED_TRANSACTION_ID_IN_ARG)

        return parameterValues[transactionIdIndex].toString()
    }
}