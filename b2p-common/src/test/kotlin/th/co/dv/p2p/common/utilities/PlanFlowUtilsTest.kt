package th.co.dv.p2p.common.utilities

import io.mockk.*
import org.junit.Before
import org.junit.Test
import th.co.dv.p2p.common.MockData
import th.co.dv.p2p.common.enums.ItemCategory
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals

class PlanFlowUtilsTest {

    private val currency = Currency.getInstance("THB")

    private val invoiceItemModel1 = MockData.mockInvoiceItemModel1.copy(itemSubTotal = 100.126.toBigDecimal())

    private val invoiceItemModel2 = MockData.mockInvoiceItemModel2.copy(itemSubTotal = 100.126.toBigDecimal())

    @Before
    fun setup() = MockKAnnotations.init(this)


    @Test
    fun `Test calculateSumOfItemsSubtotal`() {
        val planFlowUtils = spyk<PlanFlowUtils>()
        // Case ADVANCE_REDEEM
        val listAdvanceRedeem = listOf(invoiceItemModel1.copy(itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name), invoiceItemModel2)
        val expectedResultAdvanceRedeem = 200.252.toBigDecimal()
        var result = planFlowUtils.calculateSumOfItemsSubtotal(listAdvanceRedeem)
        assertEquals(expectedResultAdvanceRedeem, result)

        // Case ADVANCE_DEDUCT
        val listAdvanceDeduct = listOf(invoiceItemModel1.copy(itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name), invoiceItemModel2)
        val expectedResultAdvanceDeduct = 0.00.toBigDecimal()
        result = planFlowUtils.calculateSumOfItemsSubtotal(listAdvanceDeduct)
        assertEquals(0, expectedResultAdvanceDeduct.compareTo(result))

        // Case PROVISION
        val listProvision = listOf(invoiceItemModel1.copy(itemCategory = ItemCategory.Invoice.PROVISION.name), invoiceItemModel2)
        val expectedResultProvision = 200.252.toBigDecimal()
        result = planFlowUtils.calculateSumOfItemsSubtotal(listProvision)
        assertEquals(expectedResultProvision, result)

        // Case Normal
        val listNormal = listOf(
                invoiceItemModel1.copy(
                        unitPrice = 100.0.toBigDecimal(),
                        quantity = invoiceItemModel1.quantity!!.copy(
                                initial = 1.023.toBigDecimal()
                        )
                ),
                invoiceItemModel2.copy(
                        unitPrice = 100.0.toBigDecimal(),
                        quantity = invoiceItemModel2.quantity!!.copy(
                                initial = 1.023.toBigDecimal()
                        )
                ))
        val expectedResultNormal = BigDecimal("200.252")
        result = planFlowUtils.calculateSumOfItemsSubtotal(listNormal)
        assertEquals(0, expectedResultNormal.compareTo(result))
    }

    @Test
    fun `Test calculateSumOfItemsVatTotal`() {
        val planFlowUtils = spyk<PlanFlowUtils>()
        val groupSevenVatRate = listOf(invoiceItemModel1, invoiceItemModel2)
        val groupTreeVatRate = listOf(invoiceItemModel1.copy(vatRate = 3.0.toBigDecimal()), invoiceItemModel2.copy(vatRate = 3.0.toBigDecimal()))

        mockkObject(PlanFlowUtils)
        every { PlanFlowUtils.calculateSumOfItemsSubtotal(groupTreeVatRate) } returns 300.00.toBigDecimal()
        every { PlanFlowUtils.calculateSumOfItemsSubtotal(groupSevenVatRate) } returns 700.00.toBigDecimal()

        val expectedResult = BigDecimal(58)
        val result = planFlowUtils.calculateSumOfItemsVatTotal(groupSevenVatRate.plus(groupTreeVatRate))
        assertEquals(0, expectedResult.compareTo(result))
        unmockkObject(PlanFlowUtils)
    }
}