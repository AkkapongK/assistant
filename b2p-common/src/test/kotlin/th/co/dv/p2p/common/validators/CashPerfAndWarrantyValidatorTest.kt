package th.co.dv.p2p.common.validators

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.Test
import th.co.dv.p2p.common.TestHelper.callMethod
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.enums.MovementClass
import th.co.dv.p2p.corda.base.models.GoodsReceivedItemModel
import th.co.dv.p2p.corda.base.models.GoodsReceivedModel
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import java.math.BigDecimal
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class CashPerfAndWarrantyValidatorTest {

    private val normalGrItem = GoodsReceivedItemModel(movementClass = MovementClass.NORMAL.name)
    private val reverseGrItem = GoodsReceivedItemModel(movementClass = MovementClass.REVERSE.name)


    @Test
    fun `test InspectAfterFetchData`() {
        //case success
        var result = Try.on { CashPerfAndWarrantyValidator.inspectAfterFetchData(
                listOf(GoodsReceivedModel(goodsReceivedItems = listOf(normalGrItem, normalGrItem)))) }
        assertTrue(result.isSuccess)

        //case fail
        result =  Try.on { CashPerfAndWarrantyValidator.inspectAfterFetchData(
                listOf(GoodsReceivedModel(goodsReceivedItems = listOf(normalGrItem, reverseGrItem)))) }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Good received item's movement class must be normal only."))
    }

    @Test
    fun `Test validateBeforeDeductByGr`(){
        mockkObject(CashPerfAndWarrantyValidator, recordPrivateCalls = true)

        // case not last gr item
        every { CashPerfAndWarrantyValidator["isLastGoodsReceivedItem"](GoodsReceivedModel(), listOf(InvoiceItemModel())) } returns false
        var result = Try.on { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(BigDecimal.ZERO, BigDecimal.ZERO, GoodsReceivedModel(), listOf(InvoiceItemModel())) }
        assertTrue(result.isSuccess)

        //case is last gr item and success
        every { CashPerfAndWarrantyValidator["isLastGoodsReceivedItem"](GoodsReceivedModel(linearId = "linearId"), listOf(InvoiceItemModel(linearId = "linearId"))) } returns true
        result = Try.on { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(BigDecimal(10), BigDecimal(5),
                GoodsReceivedModel(linearId = "linearId"), listOf(InvoiceItemModel(linearId = "linearId"))) }
        assertTrue(result.isSuccess)

        //case is last gr item and fail
        result = Try.on { CashPerfAndWarrantyValidator.validateBeforeDeductByGr(BigDecimal(5), BigDecimal(10),
                GoodsReceivedModel(linearId = "linearId"), listOf(InvoiceItemModel(linearId = "linearId"))) }
        assertTrue(result.isFailure)
        assert(result.toString().contains("Invoice amount is not enough to cover cash performance guarantee. Please contact buyer."))

        unmockkObject(CashPerfAndWarrantyValidator)
    }

    @Test
    fun `Test isLastGoodsReceivedItem`(){
        // case have unused good received item
        val goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "grItemLinearId1", invoiceItemLinearId = "invoiceItemLinearId1"),
                GoodsReceivedItemModel(linearId = "grItemLinearId2", invoiceItemLinearId = null),
                GoodsReceivedItemModel(linearId = "grItemLinearId3", invoiceItemLinearId = null))
        val invoiceItem = InvoiceItemModel(linearId = "invoiceItemLinearId2", goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "grItemLinearId2")))
        var result =  callMethod<CashPerfAndWarrantyValidator, Boolean>(CashPerfAndWarrantyValidator, "isLastGoodsReceivedItem",
                GoodsReceivedModel(goodsReceivedItems = goodsReceivedItems), listOf(invoiceItem))
        assertFalse(result!!)


        // case don't have unused good received item
        val goodsReceivedItems2 = listOf(GoodsReceivedItemModel(linearId = "grItemLinearId1", invoiceItemLinearId = "invoiceItemLinearId1"),
                GoodsReceivedItemModel(linearId = "grItemLinearId2", invoiceItemLinearId = null),
                GoodsReceivedItemModel(linearId = "grItemLinearId3", invoiceItemLinearId = "invoiceItemLinearId3"))
        val invoiceItem2 = InvoiceItemModel(linearId = "invoiceItemLinearId2", goodsReceivedItems = listOf(GoodsReceivedItemModel(linearId = "grItemLinearId2")))
        result =  callMethod<CashPerfAndWarrantyValidator, Boolean>(CashPerfAndWarrantyValidator, "isLastGoodsReceivedItem",
                GoodsReceivedModel(goodsReceivedItems = goodsReceivedItems2), listOf(invoiceItem2))
        assertTrue(result!!)
    }
}

