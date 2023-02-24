package th.co.dv.p2p.common.utilities

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.utilities.search.getFullColumnName
import th.co.dv.p2p.corda.base.models.PartyModel
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.jvm.javaField
import kotlin.test.assertEquals

class StatementUtilsTest {

    @Test
    fun testGetSelectList() {
        // failed when field in dto not exist in entity
        var result = Try.on {
            StatementUtils.getSelectList(InvoiceItemDto::class, InvoiceItem::class)
        }
        assert(result.isFailure)

        // success, not send notSelectFields
        result = Try.on {
            StatementUtils.getSelectList(InvoiceItem::class, InvoiceItem::class)
        }
        assert(result.isSuccess)
        var data = result.getOrThrow()
        var selectFields = data.first
        var selectClauseFields = data.second
        assert(selectFields.containsAll(listOf(InvoiceItem::invoiceLinearId.name, InvoiceItem::bank.name, InvoiceItem::customisedFieldsUpdatedDate.name)))
        assert(selectClauseFields.containsAll(listOf(
            "invoice_item.invoice_linear_id AS invoice_item_invoice_linear_id",
            "invoice_item.bank AS invoice_item_bank",
            "invoice_item.linear_id AS invoice_item_linear_id"
        )))

        // success, with notSelectFields
        val notSelectFields = listOf(InvoiceItem::invoiceLinearId.name, InvoiceItem::bank.name)
        result = Try.on {
            StatementUtils.getSelectList(InvoiceItem::class, InvoiceItem::class, notSelectFields)
        }
        assert(result.isSuccess)
        data = result.getOrThrow()
        selectFields = data.first
        selectClauseFields = data.second
        assert(selectFields.containsAll(listOf(InvoiceItem::accounting.name, InvoiceItem::linearId.name, InvoiceItem::customisedFieldsUpdatedDate.name)))
        assert(!selectFields.containsAll(notSelectFields))
        assert(selectClauseFields.containsAll(listOf(
            "invoice_item.accounting AS invoice_item_accounting",
            "invoice_item.linear_id AS invoice_item_linear_id"
        )))
        assert(!selectClauseFields.containsAll(listOf(
            "invoice_item.invoice_linear_id AS invoice_item_invoice_linear_id",
            "invoice_item.bank AS invoice_item_bank"
        )))

        // success, addAlias = false
        result = Try.on {
            StatementUtils.getSelectList(InvoiceItem::class, InvoiceItem::class, addAlias = false)
        }
        assert(result.isSuccess)
        data = result.getOrThrow()
        selectFields = data.first
        selectClauseFields = data.second
        assert(selectFields.containsAll(listOf(InvoiceItem::invoiceLinearId.name, InvoiceItem::bank.name, InvoiceItem::customisedFieldsUpdatedDate.name)))
        assert(selectClauseFields.containsAll(listOf(
            "invoice_item.invoice_linear_id",
            "invoice_item.bank",
            "invoice_item.linear_id"
        )))
    }

    @Test
    fun testBuildSelectField() {
        mockkStatic("th.co.dv.p2p.common.utilities.search.SearchCriteriaKt")
        every { any<Field>().getFullColumnName() } returns "invoice_item.accounting"

        // case addAlias = true
        var result = StatementUtils.buildSelectField(InvoiceItem::accounting.javaField!!, true)
        assertEquals("invoice_item.accounting AS invoice_item_accounting", result)

        // case addAlias = false
        result = StatementUtils.buildSelectField(InvoiceItem::accounting.javaField!!, false)
        assertEquals("invoice_item.accounting", result)
        unmockkStatic("th.co.dv.p2p.common.utilities.search.SearchCriteriaKt")
    }


}

data class InvoiceItemDto (
    val test: String? = null
)

data class InvoiceItem (
    val invoiceLinearId: String? = null,
    var bank: PartyModel? = null,
    val customisedFieldsUpdatedDate: Date? = null,
    var accounting: PartyModel? = null,
    val linearId: String? = UUID.randomUUID().toString()
)
