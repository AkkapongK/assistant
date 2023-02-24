package th.co.dv.p2p.common.utilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import th.co.dv.p2p.common.enums.ItemCategory
import th.co.dv.p2p.common.enums.Lifecycle
import th.co.dv.p2p.common.models.ContractModel
import th.co.dv.p2p.common.utilities.Conditions.using
import th.co.dv.p2p.corda.base.models.InvoiceItemModel
import th.co.dv.p2p.corda.base.models.InvoiceModel
import th.co.dv.p2p.corda.base.models.PurchaseItemModel
import java.math.BigDecimal

object ContractUtils {

    internal val logger: Logger = LoggerFactory.getLogger(ContractUtils::class.java)
    internal val className = ContractUtils::class.java.simpleName

    /**
     * Method for validate and update advance to be deducted amount in contract based on information in invoice
     *
     * @param invoiceModel : invoice model
     * @param previousInvoiceModel: invoice model before updated data
     */
    fun List<ContractModel>.validateAndUpdateAdvanceToBeDeducted(invoiceModel: InvoiceModel, previousInvoiceModel: InvoiceModel? = null) : List<ContractModel> {
        logger.info("$className.validateAndUpdateAdvanceToBeDeducted size contractsModel : ${this.size}")

        val groupItemsAdvanceDeductWithContract = invoiceModel.invoiceItems
            .filter { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name && it.contractNumber.isNullOrBlank().not() }
            .groupBy { it.contractNumber!! }

        if (groupItemsAdvanceDeductWithContract.isEmpty()) return this

        return this.map { contractModel ->

            val invoiceItemsModel = groupItemsAdvanceDeductWithContract[contractModel.contractNumber] ?: return@map contractModel

            val advanceToBeDeducted = contractModel.advanceToBeDeducted ?: BigDecimal.ZERO

            "Advance to be deducted mount of contract ${contractModel.contractNumber} is less than zero." using (advanceToBeDeducted.isGreaterOrEqual(BigDecimal.ZERO))

            val nonCancelledItems = invoiceItemsModel.filterNot { it.lifecycle == Lifecycle.InvoiceItemLifecycle.CANCELLED.name }
            val advanceDeductionAmount = nonCancelledItems.sumByDecimal { it.itemSubTotal ?: BigDecimal.ZERO }
            val previousAdvanceItems = previousInvoiceModel?.invoiceItems?.filter { it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name && it.contractNumber == contractModel.contractNumber }
            val previousAdvanceDeductionAmount = previousAdvanceItems?.sumByDecimal { it.itemSubTotal ?: BigDecimal.ZERO }

            if (nonCancelledItems.isNotEmpty()) {
                validateUpdatedDeductionAmount(advanceToBeDeducted, advanceDeductionAmount, previousAdvanceDeductionAmount)
            }

            val finalAdvanceDeductionAmount = calculateFinalAdvanceToBeDeducted(advanceToBeDeducted, advanceDeductionAmount, previousAdvanceDeductionAmount)
            "The final advance to be deducted amount of contract ${contractModel.contractNumber} is less than zero." using finalAdvanceDeductionAmount.isGreaterOrEqual(BigDecimal.ZERO)

            contractModel.copy(advanceToBeDeducted = finalAdvanceDeductionAmount)
        }
    }

    /**
     * Method for update advance to be deduct from reject direct invoice
     *
     * @param invoiceModel : invoice model
     */
    fun List<ContractModel>.updateAdvanceToBeDeductedFromRejectDirectInvoice(invoiceModel: InvoiceModel): List<ContractModel> {
        logger.info("$className.validateAndUpdateAdvanceToBeDeducted size contractsModel : ${this.size}")

        val groupItemsAdvanceDeductWithContract = invoiceModel.invoiceItems
            .filter { it.contractNumber.isNullOrBlank().not() }
            .groupBy { it.contractNumber!! }

        if (groupItemsAdvanceDeductWithContract.isEmpty()) return this

        return this.map { contractModel ->

            val invoiceItemsModel = groupItemsAdvanceDeductWithContract[contractModel.contractNumber] ?: return@map contractModel

            val advanceToBeDeducted = contractModel.advanceToBeDeducted ?: BigDecimal.ZERO

            "Advance to be deducted mount of contract ${contractModel.contractNumber} is less than zero." using (advanceToBeDeducted.isGreaterOrEqual(BigDecimal.ZERO))

            val advanceDeductionAmount = invoiceItemsModel.sumByDecimal { it.itemSubTotal ?: BigDecimal.ZERO }

            val finalAdvanceToBeDeducted = advanceToBeDeducted.minus(advanceDeductionAmount)

            "Unable to reject an invoice since the advance payment has already deducted." using (finalAdvanceToBeDeducted.isGreaterOrEqual(BigDecimal.ZERO))

            contractModel.copy(advanceToBeDeducted = finalAdvanceToBeDeducted)
        }
    }

    /**
     * Method to calculate value for advance to be deducted amount
     * @param advanceToBeDeducted current advance to be deducted amount of contract
     * @param advanceDeductionAmount advance amount to deduct from invoice
     * @param previousAdvanceDeductionAmount advance deduction amount of previous invoice state
     */
    private fun calculateFinalAdvanceToBeDeducted(advanceToBeDeducted: BigDecimal, advanceDeductionAmount: BigDecimal, previousAdvanceDeductionAmount: BigDecimal?): BigDecimal {
        val previousAmount = previousAdvanceDeductionAmount ?: BigDecimal.ZERO
        return advanceToBeDeducted.plus(previousAmount).minus(advanceDeductionAmount)
    }

    /**
     * Method to validate updated advance to be deducted amount
     * @param advanceToBeDeducted current advance to be deducted amount of contract
     * @param advanceDeductionAmount advance amount to deduct from invoice
     * @param previousAdvanceDeductionAmount advance deduction amount of previous invoice state
     */
    private fun validateUpdatedDeductionAmount(advanceToBeDeducted: BigDecimal, advanceDeductionAmount: BigDecimal, previousAdvanceDeductionAmount: BigDecimal?) {
        val maxDeductionAmount = advanceToBeDeducted.plus(previousAdvanceDeductionAmount ?: BigDecimal.ZERO)
        "Advance deduction items amount must less than or equal $maxDeductionAmount." using advanceDeductionAmount.isLessOrEqual(maxDeductionAmount)
    }

    /**
     * Method to update contract retention amount in contract based on information in invoice
     * In case that contract does not have initialRetentionCeilingAmount, do not update it
     *
     * @param nextInvoice output invoice containing updated information of invoice
     * @param purchaseItems list of purchase items
     * @param previousInvoice input invoice containing existing information of invoice before update (Optional)
     * @return list of updated contract
     */
    fun List<ContractModel>.updateRetention(nextInvoice: InvoiceModel,
                                            purchaseItems: List<PurchaseItemModel>,
                                            previousInvoice: InvoiceModel?): List<ContractModel> {
        return this.map { contractModel ->

            if (contractModel.initialRetentionCeilingAmount == null) return@map contractModel

            val purchaseItemsInContract = purchaseItems.filter { it.contractNumber == contractModel.contractNumber }
            val purchaseItemLinearIds = purchaseItemsInContract.mapNotNull { it.linearId }

            val nextInvoicesInContract = nextInvoice.invoiceItems.filter { it.purchaseItemLinearId in purchaseItemLinearIds }
            val previousInvoicesInContract = previousInvoice?.invoiceItems?.filter { it.purchaseItemLinearId in purchaseItemLinearIds } ?: emptyList()
            val (amountToRestore, amountToDeplete) = calculateRetentionAmount(nextInvoicesInContract, previousInvoicesInContract, nextInvoice.lifecycle)

            val restoredContract = contractModel.restoreAmount(amountToRestore)
            val depletedContract = restoredContract.depleteAmount(amountToDeplete)

            depletedContract
        }
    }

    /**
     * Method to calculate amount to deplete for output invoice
     * @param nextInvoiceItems list of invoice items from output data
     * @param previousInvoiceItems list of invoice items from input data
     * @param lifecycle lifecycle of invoice header
     * @return Pair of retention amount to restore and deplete
     */
    private fun calculateRetentionAmount(nextInvoiceItems: List<InvoiceItemModel>,
                                         previousInvoiceItems: List<InvoiceItemModel>,
                                         lifecycle: String?): Pair<BigDecimal, BigDecimal> {

        val nextInvoiceRetentionAmount = nextInvoiceItems.sumByDecimal { it.retentionAmount ?: BigDecimal.ZERO }
        val previousInvoiceRetentionAmount = previousInvoiceItems.sumByDecimal { it.retentionAmount ?: BigDecimal.ZERO }

        return when (lifecycle == Lifecycle.InvoiceLifecycle.CANCELLED.name) {
            true -> nextInvoiceRetentionAmount to BigDecimal.ZERO
            false -> previousInvoiceRetentionAmount to nextInvoiceRetentionAmount
        }
    }

    /**
     * Method to restore remainingRetentionCeilingAmount in contract
     * @param amount amount to restore
     * @return restored contract
     */
    private fun ContractModel.restoreAmount(amount: BigDecimal): ContractModel {
        val initialRetentionCeilingAmount = this.initialRetentionCeilingAmount ?: BigDecimal.ZERO
        var restoredAmount = (this.remainingRetentionCeilingAmount ?: BigDecimal.ZERO).plus(amount)
        if (restoredAmount.isGreaterThan(initialRetentionCeilingAmount!!)) restoredAmount = initialRetentionCeilingAmount
        return this.copy(remainingRetentionCeilingAmount = restoredAmount)
    }

    /**
     * Method to deplete remainingRetentionCeilingAmount in contract
     * @param amount amount to deplete
     * @return depleted contract
     */
    private fun ContractModel.depleteAmount(amount: BigDecimal): ContractModel {
        val depletedAmount = (this.remainingRetentionCeilingAmount ?: BigDecimal.ZERO).minus(amount)
        "Amount after deplete must not be negative." using (depletedAmount.isGreaterOrEqual(BigDecimal.ZERO))
        return this.copy(remainingRetentionCeilingAmount = depletedAmount)
    }


    /**
     * Method for validate is invoice contains ADVANCE_DEDUCT items and matching with input contract
     */
    fun InvoiceModel.hasAdvanceItemsWithContract(contracts: List<ContractModel>) = this.invoiceItems.any {
        it.itemCategory == ItemCategory.Invoice.ADVANCE_DEDUCT.name &&
                it.contractNumber.isNullOrBlank().not() &&
                contracts.any { contract -> contract.contractNumber == it.contractNumber }
    }

    /**
     * Method for validate and calculate advanceDeductAmount
     */
    fun ContractModel.validateCalculateAdvanceDeDeduct(restoreAdvanceToBeDeducted: BigDecimal, advanceDeductionAmount: BigDecimal): ContractModel {
        val previousAdvanceToBeDeductionAmount = this.advanceToBeDeducted ?: BigDecimal.ZERO
        val previousAccumulateAdvanceRedeemAmount = this.accumulateAdvanceRedeem ?: BigDecimal.ZERO

        val newAdvanceToBeDeductionAmount = validateNewDeductAmount(restoreAdvanceToBeDeducted, advanceDeductionAmount, previousAdvanceToBeDeductionAmount)
        val newAccumulateAdvanceRedeemAmount = validateNewDeductAmount(restoreAdvanceToBeDeducted, advanceDeductionAmount, previousAccumulateAdvanceRedeemAmount)

        return this.copy(advanceToBeDeducted = newAdvanceToBeDeductionAmount, accumulateAdvanceRedeem = newAccumulateAdvanceRedeemAmount)
    }

    /**
     * Method for validate new deduct amount
     */
    private fun validateNewDeductAmount(restoreAdvanceToBeDeducted: BigDecimal, advanceDeductionAmount: BigDecimal, previousAdvanceDeductionAmount: BigDecimal): BigDecimal {
        val finalAdvanceToBeDeduct = previousAdvanceDeductionAmount
            .minus(restoreAdvanceToBeDeducted)
            .plus(advanceDeductionAmount)
        "Calculate new Advance to be deducted amount then should more than or equal zero." using (finalAdvanceToBeDeduct.isGreaterOrEqual(BigDecimal.ZERO))
        return finalAdvanceToBeDeduct
    }
}