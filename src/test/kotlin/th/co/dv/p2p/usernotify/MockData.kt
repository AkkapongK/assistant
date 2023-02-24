package th.co.dv.p2p.usernotify

import th.co.dv.p2p.common.constants.EstimatedUnitPriceFlag
import th.co.dv.p2p.common.enums.*
import th.co.dv.p2p.common.models.*
import th.co.dv.p2p.common.utilities.stringify
import th.co.dv.p2p.corda.base.domain.Quantity
import th.co.dv.p2p.corda.base.domain.WithholdingTaxCalculationPoint
import th.co.dv.p2p.corda.base.models.*
import java.math.BigDecimal
import java.time.Instant

object MockData {

    val purchaseItemsModel = listOf(
            PurchaseItemModel(
                    poNumber = "PO-001",
                    accountAssignment = "",
                    buyer = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
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
                    expectedDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    issuedDate = "2019-12-06T10:59:59.389+07:00",
                    invoiceList = "Hello_INV_01",
                    lastConfirmedBy = "pawat",
                    lastConfirmedDate = "2019-12-06T11:00:31.386+07:00",
                    lastPartyConfirmedBy = PartyModel(
                            legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                            organisation = "v",
                            organisationUnit = "SELLER"
                    ),
                    lastPartyUpdatedBy = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                            organisation = "SCG1",
                            organisationUnit = "BUYER"
                    ),
                    lastUpdatedBy = "pawat",
                    lastUpdatedDate = "2019-12-06T10:59:58.969+07:00",
                    lifecycle = "CONFIRMED",
                    linearId = "e383f6a1-f1f0-4a2f-9b04-3217080cf340",
                    materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
                    materialNumber = "1034PCK0005",
                    materialGroup = "SM00",
                    overDeliveryTolerance = BigDecimal.ZERO,
                    overDeliveryQuantity = Quantity(
                            initial = BigDecimal.ZERO,
                            consumed = BigDecimal.ZERO,
                            unit = "BAG",
                            remaining = BigDecimal.ZERO
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
                            initial = 1000.0.toBigDecimal(),
                            consumed = 200.0.toBigDecimal(),
                            unit = "BAG",
                            remaining = 800.0.toBigDecimal()
                    ),
                    poItemDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    initialDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    status = "Confirmed",
                    site = "1023",
                    siteDescription = "TPE Rayong Site3",
                    seller = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
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
                    withholdingTaxCode = "V5",
                    estimatedUnitPrice = EstimatedUnitPriceFlag.FALSE,
                    vatTriggerPoint = VatTriggerPoint.Invoice.name,
                    withholdingTaxCalculationPoint = WithholdingTaxCalculationPoint.Invoice.name,
                    itemCategory = ItemCategory.Purchase.NORMAL.name
            ))

    val purchaseItemsAdvanceModel = listOf(
            PurchaseItemModel(
                    poNumber = "PO-001",
                    accountAssignment = "",
                    buyer = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
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
                    expectedDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    issuedDate = "2019-12-06T10:59:59.389+07:00",
                    invoiceList = "Hello_INV_01",
                    lastConfirmedBy = "pawat",
                    lastConfirmedDate = "2019-12-06T11:00:31.386+07:00",
                    lastPartyConfirmedBy = PartyModel(
                            legalName = "OU=SELLER, O=SUPPLIER1, L=Bangkok, C=TH",
                            organisation = "v",
                            organisationUnit = "SELLER"
                    ),
                    lastPartyUpdatedBy = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
                            organisation = "SCG1",
                            organisationUnit = "BUYER"
                    ),
                    lastUpdatedBy = "pawat",
                    lastUpdatedDate = "2019-12-06T10:59:58.969+07:00",
                    lifecycle = "CONFIRMED",
                    linearId = "purchase-item-adb-id",
                    materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
                    materialNumber = "1034PCK0005",
                    materialGroup = "SM00",
                    overDeliveryTolerance = BigDecimal.ZERO,
                    overDeliveryQuantity = Quantity(
                            initial = BigDecimal.ZERO,
                            consumed = BigDecimal.ZERO,
                            unit = "BAG",
                            remaining = BigDecimal.ZERO
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
                    poItemUnitPrice = 71200.0.toBigDecimal(),
                    poItemUnitPriceCurrency = "THB",
                    quantity = Quantity(
                            initial = BigDecimal.ONE,
                            consumed = BigDecimal.ZERO,
                            unit = "BAG",
                            remaining = BigDecimal.ONE
                    ),
                    poItemDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    initialDeliveryDate = "2018-10-25T00:00:00.000+07:00",
                    status = "Confirmed",
                    site = "1023",
                    siteDescription = "TPE Rayong Site3",
                    seller = PartyModel(
                            legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH",
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
                    withholdingTaxCode = "V5",
                    estimatedUnitPrice = EstimatedUnitPriceFlag.FALSE,
                    vatTriggerPoint = VatTriggerPoint.Invoice.name,
                    withholdingTaxCalculationPoint = WithholdingTaxCalculationPoint.Invoice.name,
                    itemCategory = ItemCategory.Purchase.ADVANCE.name,
                    advancePaymentToBeDeducted = BigDecimal.ZERO,
                    advanceInitialAmount = 71200.0.toBigDecimal(),
                    advancePaymentRemainingAmount = 71200.0.toBigDecimal()
            ))

    val purchaseOrdersModel = listOf(
            PurchaseOrderModel(
                    vendorTaxNumber = "0745550001199",
                    paymentTermCode = "NT30",
                    paymentTermDays = 30L,
                    purchaseOrderNumber = "PO-001",
                    businessPlaceTaxNumber = "0105556176239",
                    paymentTermMonths = null,
                    customisedFields = mapOf("businessPlace" to "0021"),
                    withholdingTaxFormType = "TEST-withholdingTaxFormType",
                    withholdingTaxPayType = "TEST-withholdingTaxPayType",
                    withholdingTaxRemark = "TEST-withholdingTaxRemark",
                    linearId = "565b4cb6-f1a7-4566-9c48-d28640b86ae5"
            )
    )

    val goodsReceiveItemsModel = listOf(
            GoodsReceivedItemModel(
                    linearId = "gr-item-id",
                    externalId = "gr-item-no",
                    purchaseItemLinearId = "e383f6a1-f1f0-4a2f-9b04-3217080cf340",
                    goodsReceivedLinearId = "gr-order-id",
                    quantity = Quantity(200.0, "TON")
            ))

    val invoiceModel = InvoiceModel(
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
            currency = "THB",
            subTotal = 71200.0.toBigDecimal(),
            vatTotal = 4984.0.toBigDecimal(),
            invoiceTotal = 76184.0.toBigDecimal(),
            totalPayable = 76184.0.toBigDecimal(),
            externalId = "INV-002",
            invoiceDate = "2019-12-09T10:30:55.000Z",
            dueDate = "2019-12-09T10:30:55.000Z",
            invoiceCreatedDate = "2019-12-09T10:30:55.000Z",
            paymentDate = null,
            invoiceFinancing = 'N',
            isETaxInvoice = null,
            receiptNumber = null,
            lifecycle = Lifecycle.InvoiceLifecycle.ISSUED.name,
            postingStatus = PostingStatuses.InvoicePostingStatus.PENDING_CANCEL.name,
            referenceField1 = "1023",
            linearId = "inv-id",
            calendarKey = "P1",
            invoiceItems = listOf(
                    InvoiceItemModel(
                            purchaseOrderExternalId = "PO-001",
                            purchaseItemExternalId = "1",
                            externalId = "1",
                            materialDescription = "BIG BAG,100X100X125CM,W/LOGO",
                            invoiceLinearId = "inv-id",
                            quantity = Quantity(
                                    initial = 200.0.toBigDecimal(),
                                    unit = "BAG"
                            ),
                            currency = "THB",
                            unitPrice = 356.0.toBigDecimal(),
                            itemSubTotal = 71200.0.toBigDecimal(),
                            vatCode = "V7",
                            vatRate = 7.0.toBigDecimal(),
                            customisedFields = emptyMap(),
                            customisedFieldsUpdatedDate = "2019-12-09T10:30:55.000Z",
                            site = "1023",
                            linearId = "invoice-item-1",
                            siteDescription = "TPE Rayong Site3",
                            lifecycle = "ISSUED",
                            materialGroup = "MAT1",
                            withholdingTaxRate = 1111.111.toBigDecimal(),
                            withholdingTaxFormType = "INVOICE",
                            section = null,
                            sectionDescription = null,
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
                            goodsReceivedItems = goodsReceiveItemsModel,
                            purchaseItemLinearId = purchaseItemsModel.first().linearId,
                            status = RecordStatus.VALID.name)
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
            status = RecordStatus.VALID.name)


    val thresholdModel = ThresholdModel(
            minimumSubTotal = 0.1.toBigDecimal(),
            maximumSubTotal = 0.1.toBigDecimal(),
            minimumVatTotal = 0.1.toBigDecimal(),
            maximumVatTotal = 0.1.toBigDecimal(),
            minimumTotalAmount = 0.1.toBigDecimal(),
            maximumTotalAmount = 0.1.toBigDecimal()
    )


    val buyerModel = BuyerModel(company = CompanyModel(legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH"))
    val sellerModel = SellerModel(company = CompanyModel(legalName = "OU=BUYER, O=SCG1, L=Bangkok, C=TH"))

    val creditNoteItemModel = CreditNoteItemModel(
            linearId = "linear-item-id-001",
            externalId = "001",
            creditNoteLinearId = "linear-id-001",
            invoiceItemLinearId = invoiceModel.invoiceItems.first().linearId,
            invoiceItemExternalId = invoiceModel.invoiceItems.first().externalId,
            quantity = Quantity(BigDecimal.TEN, invoiceModel.invoiceItems.first().quantity!!.unit),
            unit = "TON",
            subTotal = 100.0.toBigDecimal(),
            lifecycle = Lifecycle.CreditNoteItemLifecycle.ISSUED.name,
            taxRate = 0.07.toBigDecimal(),
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
            unitPrice = invoiceModel.invoiceItems.first().unitPrice,
            vatTotal = 7.0.toBigDecimal(),
            issuedDate = Instant.now().stringify(),
            currency = invoiceModel.currency
    )

    val creditNoteModel = CreditNoteModel(
            linearId = "linear-id-001",
            externalId = "CN-001",
            currency = invoiceModel.currency,
            adjustmentType = AdjustmentType.QUANTITY.name,
            invoiceLinearId = invoiceModel.linearId,
            invoiceExternalId = invoiceModel.externalId,
            companyTaxNumber = invoiceModel.companyTaxNumber,
            vendorTaxNumber = invoiceModel.vendorTaxNumber,
            subTotal = 100.0.toBigDecimal(),
            vatTotal = 7.0.toBigDecimal(),
            documentCode = "80",
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
            creditNoteItems = listOf(
                    creditNoteItemModel
            ),
            lifecycle = Lifecycle.CreditNoteLifecycle.ISSUED.name,
            isETaxCreditNote = false,
            totalReceivable = 107.0.toBigDecimal(),
            total = 107.0.toBigDecimal(),
            creditNoteDate = Instant.now().stringify(),
            reason = "TEST",
            companyName = invoiceModel.companyName,
            companyBranchCode = invoiceModel.companyBranchCode,
            companyAddress = invoiceModel.companyAddress,
            companyCode = invoiceModel.companyCode,
            vendorName = invoiceModel.vendorName,
            vendorNumber = invoiceModel.vendorNumber,
            vendorBranchCode = invoiceModel.vendorBranchCode,
            vendorAddress = invoiceModel.vendorAddress,
            documentEntryDate = Instant.now().stringify(),
            status = RecordStatus.VALID.name,
            vatTriggerPoint = VatTriggerPoint.Invoice.name,
            paymentDescription = "paymentDescription",
            isEwht = null
    )

    val buyerVendorPKModel = listOf(
            BuyerVendorPKModel(buyerTaxId = "COM_TAX_001", buyerCode = "COM_001", vendorTaxId = "VENDOR_TAX_001", vendorCode = "VENDOR_001"),
            BuyerVendorPKModel(buyerTaxId = "COM_TAX_002", buyerCode = "COM_001", vendorTaxId = "VENDOR_TAX_001", vendorCode = "VENDOR_002")
    )
}