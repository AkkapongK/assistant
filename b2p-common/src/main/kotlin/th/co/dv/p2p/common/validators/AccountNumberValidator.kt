package th.co.dv.p2p.common.validators

import th.co.dv.p2p.common.constants.BANK_CODE_CONTAIN_CHAR
import th.co.dv.p2p.common.constants.validateNumber

/**
 * Class for validate account number
 */
object AccountNumberValidator {

    /**
     * Method for validate account number
     * @param bankAccountNumber: bank account number
     * @param bankCode: bank code
     */
    fun validateAccountNumber(bankAccountNumber: String, bankCode: String?) :Boolean{
        return BANK_CODE_CONTAIN_CHAR.contains(bankCode) || bankAccountNumber.matches(validateNumber.toRegex())
    }
}


