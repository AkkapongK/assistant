package th.co.dv.p2p.corda.base.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import net.corda.core.serialization.CordaSerializable

/**
 * Returned in queries
 * A PageModel contains:
 *  1) [page]: The current Page number which the results are on
 *  2) [rows]: The actual records in current page
 *  3) [pageSize]: The count of [rows]
 *  4) [totalRecordsAvailable]: The total records available matching the criteria to allow you to further paginate accordingly
 *
 *  TODO: need to rename [totalRecordsAvailable] to totalRecordsAvailable to better reflect its purpose.
 *        Not to confuse with [pageSize] which is the total records in current page
 *
 *  For example, if [pageSize] based on actual number of [rows] is 200, and [totalRecordsAvailable] is 208
 *  the API user may want to send another query for pageNumber #2 to get the remaining 8 records
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@CordaSerializable
data class PageModel<T>(
        val page: Int = 1,
        val pageSize: Int,
        @JsonProperty("totalRecords")
        val totalRecordsAvailable: Long,
        val rows: List<T>){

    constructor(page: Int, rows: List<T>, totalRecordsAvailable: Long) : this(
            page = page,
            pageSize = rows.size,
            totalRecordsAvailable = totalRecordsAvailable,
            rows = rows)

    constructor(rows: List<T>) : this(
            pageSize = rows.size,
            totalRecordsAvailable = rows.size.toLong(),
            rows = rows)
}