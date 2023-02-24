package th.co.dv.p2p.common.utilities

import io.mockk.*
import junit.framework.TestCase.assertTrue
import org.junit.Test
import th.co.dv.p2p.common.Try
import th.co.dv.p2p.common.constants.CORDA_UI
import th.co.dv.p2p.common.constants.cannotCastToLong
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.VatTriggerPoint
import th.co.dv.p2p.common.models.SellerModel
import th.co.dv.p2p.common.models.ThresholdModel
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PostingDetailModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import java.lang.reflect.Field
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.persistence.Id
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommonUtilsTest {

    @Test
    fun testConvertStringToLong() {

        var value: String? = null
        // Case : null
        var result = trySilently {
            value.convertToLong()
        }

        assertTrue(result.exception!!.message!!.contains(cannotCastToLong))

        // Case: Cannot Cast 1
        value = "XXXXX"
        result = trySilently {
            value.convertToLong()
        }

        assertTrue(result.exception!!.message!!.contains(cannotCastToLong))

        // Case: Cannot Cast 2
        value = "1.0"
        result = trySilently {
            value.convertToLong()
        }

        assertTrue(result.exception!!.message!!.contains(cannotCastToLong))

        // Case: Cannot Cast 3
        value = "1L"
        result = trySilently {
            value.convertToLong()
        }

        assertTrue(result.exception!!.message!!.contains(cannotCastToLong))

        // Case: Success
        value = "1"
        val successResult = value.convertToLong()

        assertEquals(1L, successResult)
    }

    @Test
    fun `test copyPropsFrom`() {
        val classEntity = listOf(
                ClassEntity(interestRate = BigDecimal("13.1234"))
        )

        val classModel = listOf(
                ClassModel(interestRate = BigDecimal("13.1234").toString())
        )

        // case entity to model
        val result = classEntity.map {
            val model = ClassModel()
            model.copyPropsFrom(it)
            model
        }

        assertEquals(classModel.first().interestRate, result.first().interestRate)

        // case model to entity
        val result2 = classModel.map {
            val entity = ClassEntity()
            entity.copyPropsFrom(it)
            entity
        }

        assertTrue(classEntity.first().interestRate?.compareTo(result2.first().interestRate) == 0)
    }

    @Test
    fun `Test addOtherDataToTargetField`() {
        val data1 = mutableMapOf(
                "linear_id" to "06a6807f-68d6-43a9-9fc8-8b03ef06b36e",
                "company_code" to "400",
                "delegated_authorities" to mutableListOf<Map<String, Any>>()
        )

        val data2 = mutableMapOf(
                "linear_id" to "06a6807f-68d6-43a9-9fc8-8b03ef06b36e",
                "company_code" to "400",
                "delegated_authorities" to mutableListOf<Map<String, Any>>()
        )
        val otherData = listOf(
                mapOf(
                        "linear_id" to "06a6807f-68d6-43a9-9fc8-8b03ef06b36e",
                        "assignee" to "test",
                        "company_code" to "400"
                )
        )
        val allData = mutableMapOf(
                "06a6807f-68d6-43a9-9fc8-8b03ef06b36e" to data1,
                "06a6807f-68d6-43a9-9fc8-8b03ef06b36e-400" to data2
        )
        // Case single id key
        allData.addOtherDataToTargetField(otherData, listOf("linear_id"), "delegated_authorities")
        assertEquals(otherData, allData["06a6807f-68d6-43a9-9fc8-8b03ef06b36e"]?.get("delegated_authorities"))

        // Case multi id key
        allData.addOtherDataToTargetField(otherData, listOf("linear_id", "company_code"), "delegated_authorities")
        assertEquals(otherData, allData["06a6807f-68d6-43a9-9fc8-8b03ef06b36e-400"]?.get("delegated_authorities"))

    }

    @Test
    fun testDefinePriorityByAppId() {
        // Case appId be in uiAppIds
        val uiAppIds = listOf("app-01", "app-02")

        var result = definePriorityByAppId("app-02", uiAppIds)
        assertEquals(5, result)

        // Case appId is CORDA_UI
        result = definePriorityByAppId(CORDA_UI, uiAppIds)
        assertEquals(5, result)

        // Case uiAppIds is empty and appId is CORDA_UI
        result = definePriorityByAppId(CORDA_UI, emptyList())
        assertEquals(5, result)

        // Case uiAppIds is empty and appId is MOCK value
        result = definePriorityByAppId("MOCK", emptyList())
        assertEquals(1, result)

        // Case appId nit be in uiAppIds
        result = definePriorityByAppId("app-03", uiAppIds)
        assertEquals(1, result)

    }

    @Test
    fun testToAbs() {

        val thresholdModel = ThresholdModel(
                minimumSubTotal = (-0.1).toBigDecimal(),
                maximumSubTotal = 0.1.toBigDecimal(),
                minimumVatTotal = 0.1.toBigDecimal(),
                maximumVatTotal = 0.1.toBigDecimal(),
                minimumTotalAmount = 0.1.toBigDecimal(),
                maximumTotalAmount = 0.1.toBigDecimal(),
                minimumInvHeaderDiffPercent = (-10.0).toBigDecimal(),
                maximumInvHeaderDiffPercent = 10.0.toBigDecimal(),
                minimumItemSubTotal = 10.0.toBigDecimal(),
                maximumItemSubTotal = 10.0.toBigDecimal())

        val result = thresholdModel.toAbs()

        assertEquals(0.1.toBigDecimal(), result.minimumSubTotal)
        assertEquals(0.1.toBigDecimal(), result.maximumSubTotal)
        assertEquals(0.1.toBigDecimal(), result.minimumVatTotal)
        assertEquals(0.1.toBigDecimal(), result.maximumVatTotal)
        assertEquals(0.1.toBigDecimal(), result.minimumTotalAmount)
        assertEquals(0.1.toBigDecimal(), result.maximumTotalAmount)
        assertEquals(10.0.toBigDecimal(), result.minimumInvHeaderDiffPercent)
        assertEquals(10.0.toBigDecimal(), result.maximumInvHeaderDiffPercent)
        assertEquals(10.0.toBigDecimal(), result.minimumItemSubTotal)
        assertEquals(10.0.toBigDecimal(), result.maximumItemSubTotal)
    }

    @Test
    fun `test getRelateField`() {
        val parameters = mapOf("dueDateFrom" to "2/2/2020", "interestRate" to "interestRate")
        val result = getRelateField<ClassModel>(parameters)
        assertEquals(1, result.size)
    }

    @Test
    fun testUpdateRdSubmittedDate() {

        val invoiceList = listOf(InvoiceModel(externalId = "inv-001"), InvoiceModel(externalId = "inv-002"))
        val result = invoiceList.updateRdSubmittedDate()
        assertTrue(result.all { it.rdSubmittedDate != null })
    }


    @Test
    fun testFilterDocumentByRdActiveAndEndDate() {

        val today = Instant.now()
        val mockSellerModelList = listOf(
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.plusMonths(1)), rdActiveStartDate = Date.from(today.minusDays(1))),
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.plusMonths(1)), rdActiveStartDate = Date.from(today.minusDays(1))),
                SellerModel(taxId = "001", rdActiveEndDate = Date.from(today.minusDays(3)), rdActiveStartDate = Date.from(today.minusDays(1))))
        val mockInvoiceList = listOf(
                InvoiceModel(externalId = "inv-001", vatTriggerPoint = VatTriggerPoint.Payment.name, vendorTaxNumber = "001", invoiceDate = today.stringify()),
                InvoiceModel(externalId = "inv-002", vatTriggerPoint = VatTriggerPoint.Invoice.name, vendorTaxNumber = "001", invoiceDate = today.minusDays(5).stringify()),
                InvoiceModel(externalId = "inv-003", vatTriggerPoint = VatTriggerPoint.None.name, vendorTaxNumber = "001", invoiceDate = today.plusMonths(5).stringify()))

        val result = mockInvoiceList.filterDocumentByRdActiveAndEndDate(InvoiceModel::invoiceDate.name, mockSellerModelList)

        assertTrue(result.isEmpty().not())
        assertEquals(1, result.size)
        assertEquals("inv-001", result.single().externalId)

    }

    @Test
    fun `Test retry`() {
        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")

        // Case failed all time
        every { definePriorityByAppId(any(), any()) } throws Exception("Test Exception")

        var result = Try.on {
            retry(3, 3000) { definePriorityByAppId("appId", listOf("uiAppId")) }
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Test Exception"))
        verify(exactly = 3) { definePriorityByAppId(any(), any()) }
        clearAllMocks(answers = false)

        // Case failed some time and then success
        every { definePriorityByAppId(any(), any()) } throws Exception("Test Exception") andThen (5)

        result = Try.on {
            retry(3, 3000) { definePriorityByAppId("appId", listOf("uiAppId")) }
        }
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrThrow())
        verify(exactly = 2) { definePriorityByAppId(any(), any()) }

        unmockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
    }

    @Test
    fun `Test SelectSponsor`() {
        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
        mockkObject(SponsorContextHolder)
        var needToGetSponsor = true
        every { SponsorContextHolder.getCurrentSponsor() } returns "IRPC"

        var result = selectSponsorForBankQueue(needToGetSponsor)
        assertEquals("IRPC", result)

        needToGetSponsor = false
        result = selectSponsorForBankQueue(needToGetSponsor)
        assertEquals("BANK", result)

        unmockkObject(SponsorContextHolder)
        unmockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
    }

    @Test
    fun `Test Any getFieldId`() {
        mockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")

        val mockField = mockk<Field>()
        every { DataClassWithAnnotationId::class.java.getFieldId() } returns mockField

        val dataClassWithAnnotationId = DataClassWithAnnotationId()
        val result = dataClassWithAnnotationId.getFieldId()
        assertEquals(mockField, result)

        unmockkStatic("th.co.dv.p2p.common.utilities.CommonUtilsKt")
    }

    @Test
    fun `Test Class getFieldId`() {
        // Case found annotation id
        val dataClassWithAnnotationId = DataClassWithAnnotationId()
        var result = dataClassWithAnnotationId::class.java.getFieldId()
        assertNotNull(result)
        assertEquals(dataClassWithAnnotationId::class.java.getDeclaredField("idField"), result)

        // Case not found annotation id
        val invoiceModel = InvoiceModel()
        result = invoiceModel.getFieldId()
        assertNull(result)
    }

    @Test
    fun testSetScaleAmountField() {
        val fieldRequiredSetScale = listOf(
                PurchaseItemModel::amount.name,
                PurchaseItemModel::companyName.name
        )
        val purchaseItem = PurchaseItemModel(amount = BigDecimal(99.9999999999), poItemUnitPrice = BigDecimal(99.9999999999), companyName = "companyName")
        purchaseItem.setScaleAmountField(fieldRequiredSetScale)
        assertEquals(0, 100.0.toBigDecimal().compareTo(purchaseItem.amount))
        assertEquals(0, BigDecimal(99.9999999999).compareTo(purchaseItem.poItemUnitPrice))
        assertEquals("companyName", purchaseItem.companyName)
    }

    @Test
    fun `test checkInvoiceCategory`(){
        var invoiceItems = listOf(
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.ADVANCE_REDEEM.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.PROVISION.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name)
        )
        var result = InvoiceModel(invoiceItems = invoiceItems).checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.ADVANCE_REDEEM, result)

        invoiceItems = listOf(
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.ADVANCE_DEDUCT.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.PROVISION.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name)
        )
        result = InvoiceModel(invoiceItems = invoiceItems).checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.ADVANCE_DEDUCT, result)

        invoiceItems = listOf(
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.PROVISION.name),
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name)
        )
        result = InvoiceModel(invoiceItems = invoiceItems).checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.PROVISION, result)

        invoiceItems = listOf(
            InvoiceItemModel(itemCategory = ItemCategory.Invoice.NORMAL.name)
        )
        result = InvoiceModel(invoiceItems = invoiceItems).checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.NORMAL, result)

        invoiceItems = emptyList()
        result = InvoiceModel(invoiceItems = invoiceItems).checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.NORMAL, result)

        result = null.checkInvoiceCategory()
        assertEquals(ItemCategory.Invoice.NORMAL, result)
    }

    @Test
    fun testFindSponsorByCommand() {
        var command = "[--spring.batch.job.enabled=true, --spring.batch.job.names=JOB_KNOCKING, sponsor=Minor, batchdate=20210518175716, companyTaxNumber=0107536000889]"
        var result = findSponsorByCommand(command)
        assertEquals("Minor", result)

        command = "[--spring.batch.job.enabled=true, --spring.batch.job.names=JOB_KNOCKING, sponsor=Minor]"
        result = findSponsorByCommand(command)
        assertEquals("Minor", result)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testConvertPostingDetailToCustomisedFields() {
        val caseNotRetention = listOf(
            PostingDetailModel(
                id = 1L,
                fiDocType = "NORMAL",
                fiDocNumber = "001",
                fiDocFiscalYear = "2021",
                fiDocHeaderText = "H001",
                livDocNumber = "D001",
                message = "Test 01"
            ),  PostingDetailModel(
                id = 2L,
                fiDocType = "NORMAL",
                fiDocNumber = "002",
                fiDocFiscalYear = "2021",
                fiDocHeaderText = "H002",
                livDocNumber = "D002",
                message = "Test 02"
            )
        )

        // Case there is no fi foc retention
        var result = covertPostingDetailToCustomisedFields(caseNotRetention)
        var liv = result["LIV"] as Map<String, Any>
        assertNotNull(liv)
        assertEquals("Test 01", liv["messageText"])
        assertEquals("001", liv["accountingDocumentNumber"])
        assertEquals("2021", liv["fiscalYear"])
        assertEquals("H001", liv["FIDocHeaderText"])
        assertEquals("D001", liv["LIVDocumentNo"])
        assertNull(liv["FIDocRetention"])

        // Case there is fi foc retention
        val caseRetention = listOf(
            PostingDetailModel(
                id = 1L,
                fiDocType = "NORMAL",
                fiDocNumber = "001",
                fiDocFiscalYear = "2021",
                fiDocHeaderText = "H001",
                livDocNumber = "D001",
                message = "Test 01"
            ),  PostingDetailModel(
                id = 2L,
                fiDocType = "RETENTION",
                fiDocNumber = "002",
                fiDocFiscalYear = "2021",
                fiDocHeaderText = "H002",
                livDocNumber = "D002",
                message = "Test 02"
            )
        )

        result = covertPostingDetailToCustomisedFields(caseRetention)
        println(result)
        liv = result["LIV"] as Map<String, Any>
        assertNotNull(liv)
        assertEquals("Test 01", liv["messageText"])
        assertEquals("001", liv["accountingDocumentNumber"])
        assertEquals("2021", liv["fiscalYear"])
        assertEquals("H001", liv["FIDocHeaderText"])
        assertEquals("D001", liv["LIVDocumentNo"])
        assertEquals("002", liv["FIDocRetention"])
    }
}

class ClassModel(
        var interestRate: String? = null
)

class ClassEntity(
        var interestRate: BigDecimal? = null
)

private data class DataClassWithAnnotationId(
        @Id
        val idField: String? = null,
        val otherField: String? = null
)
