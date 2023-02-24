package th.co.dv.p2p.common

import th.co.dv.p2p.common.constants.EstimatedUnitPriceFlag
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.models.BuyerModel
import th.co.dv.p2p.common.models.BuyerVendorModel
import th.co.dv.p2p.common.models.CompanyModel
import th.co.dv.p2p.common.models.SellerModel
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import java.time.Instant

class MockData {
    companion object {
        val mockBuyerModel = BuyerModel(
                company = CompanyModel(
                        legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        whtFormType = "BWHTFORM"
                )

        )
        val mockSellerModel = SellerModel(
                company = CompanyModel(
                        legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                        whtFormType = "SWHTFORM"
                )
        )
        val mockBuyerVendorModel = BuyerVendorModel(paymentCalendar = "C1")

        val mockPurchaseItemModel = PurchaseItemModel(
                poNumber = "PO-001",
                accountAssignment = "",
                buyer = PartyModel(
                        legalName =  "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                businessPlace = "0021",
                businessPlaceTaxNumber = "0105556176239",
                businessPlaceDescription = "TPE Rayong Site3",
                businessPlaceStreet1 = "271 ถนนสุขุมวิท ตำบลมาบตาพุด",
                businessPlaceStreet2 = "",
                businessPlaceStreet3 = "",
                businessPlaceStreet4 = "",
                businessPlaceCity = "กรุงเทพ",
                businessPlaceDistrict = "อำเภอเมืองระยอง",
                businessPlacePostalCode = "21150",
                businessPlacePostalTelephone = "",
                businessPlaceDepartment = "",
                customisedFields = emptyMap(),
                customisedFieldsUpdatedDate = "2019-12-06T10:59:58.969+07:00",
                companyCode = "0100",
                companyBranchCode = "00002",
                companyName = "บริษัท ไทยโพลิเอททีลีน จำกัด",
                calendarKey = "P1",
                estimatedUnitPrice = EstimatedUnitPriceFlag.FALSE,
                expectedDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                issuedDate = "2019-12-06T10:59:59.389+07:00",
                invoiceList = "Hello_INV_01",
                lastConfirmedBy = "pawat",
                lastConfirmedDate = "2019-12-06T11:00:31.386+07:00",
                lastPartyConfirmedBy = PartyModel(
                        legalName =  "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                        organisation = "v",
                        organisationUnit = "SELLER"
                ),
                lastPartyUpdatedBy = PartyModel(
                        legalName =  "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                lastUpdatedBy = "pawat",
                lastUpdatedDate = "2019-12-06T10:59:58.969+07:00",
                lifecycle = Lifecycle.PurchaseItemLifecycle.CONFIRMED.name,
                linearId = "e383f6a1-f1f0-4a2f-9b04-3217080cf340",
                materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
                materialNumber = "1034PCK0005",
                materialGroup = "SM00",
                overDeliveryTolerance = BigDecimal.ZERO,
                overDeliveryQuantity = Quantity(
                        initial = BigDecimal.ZERO,
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                ),
                paymentTermDescription = "",
                purchasingOrg = "0901",
                purchasingGroupName = "Ms.Supawan B.",
                purchasingGroupTel = "66 3891 1473",
                purchasingEmail = "Supawanb@scg.com",
                purchaseOrderLinearId = "565b4cb6-f1a7-4566-9c48-d28640b86ae5",
                paymentTermCode = "NT30",
                paymentTermDays = 30,
                purchaseRequestNumber = "1002003491",
                purchaseRequestItem = "1",
                purchasingGroup = "A06",
                poItemNo = "1",
                poItemUnitPrice = 356.00.toBigDecimal(),
                poItemUnitPriceCurrency = "THB",
                quantity = Quantity(
                        initial = 8000.0.toBigDecimal(),
                        consumed = BigDecimal.ZERO,
                        unit = "BAG"
                ),
                poItemDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                initialDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                status = "Confirmed",
                site = "1023",
                siteDescription = "TPE Rayong Site3",
                seller = PartyModel(
                        legalName =  "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                taxCode = "V7",
                taxRate = 7.0.toBigDecimal(),
                unitDescription = "Bag",
                underDeliveryTolerance = BigDecimal.ZERO,
                vendorNumber = "0001042940",
                vendorTaxNumber = "0745550001199",
                vendorEmail = "vasukitd@csloxinfo.com",
                vendorBranchCode = "00000",
                vendorName = "BANGKOK POLYBULK CO.,LTD.",
                vendorStreet1 = "94/2 ม.8 ซ.สุขสวัสดิ์ 72 ถ.สุขสวัสดิ์",
                vendorStreet2 = "",
                vendorStreet3 = "",
                vendorStreet4 = "",
                vendorCity = "จ.สมุทรปราการ",
                vendorDistrict = "ต.บางครุ อ.พระประแดง",
                vendorPostalCode = "74110",
                vendorTelephone = "024641670-2",
                withholdingTaxIncomeType = "test",
                withholdingTaxIncomeDescription = "test",
                withholdingTaxPercent = 5.0.toBigDecimal(),
                withholdingTaxCode = "V5"
        )

        val mockPurchaseItemModel1 = mockPurchaseItemModel.copy()
        val mockPurchaseItemModel2 = mockPurchaseItemModel.copy(linearId = "e3rfw6a1-f1f0-4a2f-9b04-3217080rwekk", poItemNo = "2")

        val mockPurchaseOrderModel = PurchaseOrderModel(
                vendorBranchCode = "00000",
                currency = "THB",
                vendorTaxNumber = "0745550001199",
                vendorNumber = "0001042940",
                companyCode = "0100",
                paymentTermCode = "NT30",
                paymentTermDays = 30L,
                purchaseOrderNumber = "PO-001",
                businessPlaceTaxNumber = "0105556176239",
                paymentTermMonths = null,
                customisedFields = mapOf("businessPlace" to "0021"),
                withholdingTaxFormType = "TEST-withholdingTaxFormType",
                withholdingTaxPayType = "TEST-withholdingTaxPayType",
                withholdingTaxRemark = "TEST-withholdingTaxRemark",
                purchaseItems = listOf(mockPurchaseItemModel1, mockPurchaseItemModel2),
                lifecycle = Lifecycle.PurchaseOrderLifecycle.CONFIRMED.name,
                linearId = "565b4cb6-f1a7-4566-9c48-d28640b86ae5",
                companyBranchCode = "00002",
                issuedDate = Instant.now().toString(),
                retentionPercent = BigDecimal.ZERO,
                seller = PartyModel(
                        legalName =  "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                buyer = PartyModel(
                        legalName =  "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                paymentTermDescription = "payment term 30 days",
                documentEntryDate = "14/12/2019",
                companyBranchName = "branch name001",
                businessPlaceCity = "Bangkok",
                businessPlaceCountry = "TH",
                businessPlaceDepartment = "operation",
                businessPlaceDistrict = "Klong Toey",
                businessPlaceEmail = "operation@mockbizz.co.th",
                businessPlaceOfficerEmail = "operator@mockbizz.co.th",
                businessPlaceOfficerName = "foo bar",
                businessPlaceOrganization = "mock bizz org",
                businessPlacePostalCode = "10110",
                businessPlaceAddress1 = "123/456",
                vendorBranchName = "vendor place0010",
                vendorCity = "Chiang Mai",
                vendorCountry = "TH",
                vendorDepartment = "management",
                vendorDistrict = "Meung",
                vendorEmail = "management@mockseller.co.th",
                vendorName = "Mock seller",
                vendorOfficerEmail = "manager@mockseller.co.th",
                vendorOfficerName = "bar foo",
                vendorOrganization = "mock seller org",
                vendorPostalCode = "43210",
                vendorAddress1 = "333/444",
                vendorAddress2 = "vend addr2",
                vendorTelephone = "0090000098"
        )


        val mockInvoiceItemModel1 = InvoiceItemModel(
            purchaseOrderExternalId = mockPurchaseItemModel1.poNumber,
            purchaseItemExternalId = mockPurchaseItemModel1.poItemNo,
            externalId = "1",
            materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
            quantity = Quantity(
                initial = 200.0.toBigDecimal(),
                unit = "BAG"
            ),
            buyer = PartyModel(
                legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                organisation = "SCG1",
                organisationUnit = "BUYER"
            ),
            seller = PartyModel(
                legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                organisation = "SUPPLIER1",
                organisationUnit = "SELLER"
            ),
            currency = "THB",
            unitPrice = 356.0.toBigDecimal(),
            itemSubTotal = 71200.0.toBigDecimal(),
            vatCode = "V7",
            vatRate = 7.0.toBigDecimal(),
            customisedFields = emptyMap(),
            customisedFieldsUpdatedDate = "2019-12-09T10:30:55.000Z",
            site = "1023",
            siteDescription = "TPE Rayong Site3",
            section = null,
            sectionDescription = null,
            purchaseItemLinearId = mockPurchaseItemModel1.linearId,
            itemCategory = ItemCategory.Invoice.NORMAL.name
        )

        val mockInvoiceItemModel2 = mockInvoiceItemModel1.copy(
                externalId = "2",
                purchaseOrderExternalId = mockPurchaseItemModel2.poNumber,
                purchaseItemExternalId = mockPurchaseItemModel2.poItemNo,
                purchaseItemLinearId = mockPurchaseItemModel2.linearId
        )

        val mockInvoiceModel = InvoiceModel(
                linearId = "12332123",
                vendorNumber = "0001042940",
                vendorBranchCode = "00000",
                vendorName = "BANGKOK POLYBULK CO.,LTD.",
                vendorTaxNumber = "0745550001199",
                vendorAddress = "94/2 ม.8 ซ.สุขสวัสดิ์ 72 ถ.สุขสวัสดิ์ ต.บางครุ อ.พระประแดง จ.สมุทรปราการ 74110งเทพฯ 10600",
                companyCode = "0100",
                companyName = "บริษัท ไทยโพลิเอททีลีน จำกัด",
                companyTaxNumber = "0105556176239",
                companyBranchCode = "00002",
                companyBranchName = "TPE Rayong Site3",
                companyAddress = "271 ถนนสุขุมวิท ตำบลมาบตาพุด อำเภอเมืองระยอง กรุงเทพ 21150",
                businessPlace = "0021",
                paymentTermCode = "NT30",
                paymentTermDays = 30,
                paymentTermDesc = "",
                lifecycle = "ISSUED",
                currency = "THB",
                subTotal = 71200.0.toBigDecimal(),
                vatTotal = 4984.0.toBigDecimal(),
                invoiceTotal = 76184.0.toBigDecimal(),
                externalId = "INV-001",
                invoiceDate = "2019-12-09T10:30:55.000Z",
                dueDate = "2019-12-09T10:30:55.000Z",
                invoiceCreatedDate = "2019-12-09T10:30:55.000Z",
                paymentDate = null,
                invoiceFinancing = 'N',
                totalPayable = 76184.0.toBigDecimal(),
                calendarKey = "C1",
                isETaxInvoice = null,
                receiptNumber = null,
                referenceField1 = "1023",
                invoiceItems = listOf(mockInvoiceItemModel1, mockInvoiceItemModel2),
                buyer = PartyModel(
                        legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                        organisation = "SCG1",
                        organisationUnit = "BUYER"
                ),
                seller = PartyModel(
                        legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                        organisation = "SUPPLIER1",
                        organisationUnit = "SELLER"
                ))
    }
}