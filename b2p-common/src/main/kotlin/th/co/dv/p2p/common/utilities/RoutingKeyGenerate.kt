package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.utilities.AuthorizationUtils.logger

/**
 * Service for generate routing key that we send to external RabbitMQ
 * we need to generate routing key separate by customer company
 * i.e. SCG, MINT,IRPC, etc
 * So we get the value for Master data service (Sponsor name)
 */

object RoutingKeyGenerate {

    /**
     * generate routing key for sending external server
     * with pattern {bank}.{queueName} (UPPERCASE)
     */
    fun genRoutingKeyForExternalQueue(queue: String, sponsor: String? = null): String {
        logger.info("RoutingKeyGenerate.genRoutingKeyForExternalQueue  queue=$queue")
        val routingKey =  "$sponsor.$queue".uppercase()
        logger.info("RoutingKeyGenerate.genRoutingKeyForExternalQueue routingKey=$routingKey")
        return routingKey
    }

}