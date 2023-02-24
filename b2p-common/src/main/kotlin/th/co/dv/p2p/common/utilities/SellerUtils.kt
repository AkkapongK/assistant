package th.co.dv.p2p.common.utilities

import th.co.dv.p2p.common.models.SellerModel
import th.co.dv.p2p.common.utilities.Conditions.using

/**
 * Method for filter seller with rd date condition
 */
fun List<SellerModel>.filterSellerWithRdDate(): List<SellerModel> {
    "Can not found seller." using (this.isNullOrEmpty().not())
    return this.filter { (it.rdActiveStartDate != null && it.rdActiveStartDate!!.before(it.rdActiveEndDate)) }
}