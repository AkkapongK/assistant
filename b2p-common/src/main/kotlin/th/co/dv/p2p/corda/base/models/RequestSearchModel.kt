package th.co.dv.p2p.corda.base.models

import net.corda.core.serialization.CordaSerializable

/**
 * Search Model For Request to enable the front end to query By:-
 * the fields available inside this state
 */
@CordaSerializable
data class RequestSearchModel(
        val linearIds : List<String>? = null,
        val requestDocumentNumbers : List<String>? = null,
        val exactlyRequestDocumentNumbers : String? = null,
        val referenceDocumentNumbers : List<String>? = null,
        val exactlyReferenceDocumentNumber : String? = null,
        val referenceLinearId : String? = null,
        val companyTaxNumber : String? = null,
        val companyNames : List<String>? = null,
        val companyName : String? = null,
        val vendorTaxNumber : String? = null,
        val vendorNames : List<String>? = null,
        val vendorName : String? = null,
        val initiator : String? = null,
        val requestTypes : List<String>? = null,
        val requestSubTypes : List<String>? = null,
        val statuses : List<String>? = null,
        val documentNumbers : List<String>? = null,
        val documentEntryDateFrom: String? = null,
        val documentEntryDateTo: String? = null,
        val parentRequestNumber: String? = null,
        val parentRequestIds: List<String>? = null,
        val returnChildRequest: Boolean? = false,
        val returnRequestItems: Boolean = true,
        val sortField: String = "externalId",
        val sortOrder: Int = 1,
        val pageNumber: Int = 1,
        val pageSize: Int = 500
)
