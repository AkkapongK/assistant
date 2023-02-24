package th.co.dv.p2p.common.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import th.co.dv.p2p.common.base.services.BaseProducer
import th.co.dv.p2p.common.constants.REQUEST_IN_COMMAND
import th.co.dv.p2p.common.constants.TRANSACTION_ID
import th.co.dv.p2p.common.constants.UNDERSCORE
import th.co.dv.p2p.common.enums.Services
import th.co.dv.p2p.common.kafka.KafkaTopicConstant.Companion.REQUEST_INPUT
import th.co.dv.p2p.common.models.LogRequestInModel
import th.co.dv.p2p.common.utilities.DataSourceContextHolder
import th.co.dv.p2p.common.utilities.MapContextHolder
import th.co.dv.p2p.common.utilities.RequestUtils.getBody
import th.co.dv.p2p.common.utilities.RequestUtils.getHeaders
import th.co.dv.p2p.common.utilities.RestServiceUtilities.HEADER_SPONSOR
import th.co.dv.p2p.common.utilities.SponsorContextHolder
import th.co.dv.p2p.common.utilities.stringify
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class BaseInboundRequestInterceptor(private val services: Services, private val producerService: BaseProducer) : AsyncHandlerInterceptor {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(BaseInboundRequestInterceptor::class.java)
        private val className = BaseInboundRequestInterceptor::class.java.simpleName
    }

    private val methodToLog = listOf(
        HttpMethod.POST.name,
        HttpMethod.PUT.name,
        HttpMethod.DELETE.name
    )

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
//        SponsorContextHolder.clear()
//        DataSourceContextHolder.clear()
//
//        val sponsor = request.getHeader(HEADER_SPONSOR)
//        val transactionId = UUID.randomUUID().toString()
//        MapContextHolder.setMap(TRANSACTION_ID, transactionId)
//
//        if (logger.isDebugEnabled) logger.debug("$className.preHandle sponsor: $sponsor")
//        SponsorContextHolder.setSponsor(sponsor)
//        DataSourceContextHolder.setCurrentDb(sponsor.lowercase())
//
//        putRequestToKafka(
//            request = request,
//            transactionId = transactionId,
//            sponsor = sponsor
//        )

        return super.preHandle(request, response, handler)
    }


    /**
     * Method to put request to Kafka for monitoring
     */
    open protected fun putRequestToKafka(request: HttpServletRequest, transactionId: String, sponsor: String) {
        val url = request.requestURL
        val method = request.method
        val headers = getHeaders(request)
        val requestParams = request.queryString
        val body = getBody(request)

        // produce to kafka
        if (methodToLog.contains(method).not()) return

        val data = LogRequestInModel(
                url = url.toString(),
                method = method,
                headers = headers,
                queryString = requestParams,
                body = body,
                request_timestamp = Instant.now().stringify()
        )

        // producerService.streamEventMessage(
        //     transactionId = transactionId,
        //     topic = services.name + UNDERSCORE + REQUEST_INPUT,
        //     sponsor = sponsor,
        //     service = services.name,
        //     command = REQUEST_IN_COMMAND,
        //     data = data
        // )
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        super.postHandle(request, response, handler, modelAndView)
    }
}
