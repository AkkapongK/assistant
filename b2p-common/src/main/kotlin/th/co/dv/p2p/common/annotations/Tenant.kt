package th.co.dv.p2p.common.annotations


/**
 * Annotation for define state want to apply data tenant
 *
 * - State has a buyer field only
 *   @Tenant(buyer="buyerField , seller="")
 *
 * - State has a seller field only
 *   @Tenant(buyer="" , seller="sellerField)
 *
 * - State have both of buyer and seller fields
 *   @Tenant(buyer="buyerField" , seller="sellerField")
 *
 * - State have both of buyer and seller fields but want apply buyer field only
 *   @Tenant(buyer="buyerField" , seller="")
 *
 * - State have both of buyer and seller fields but want apply seller field only
 *   @Tenant(buyer="" , seller="sellerField")
 *
 * - State use buyer and seller field as a same field
 *   @Tenant(buyer="sameField" , seller="sameField")
 *
 * @param buyer : buyer field for apply data tenant (e.g. companyTaxNumber)
 * @param seller : seller field for apply data tenant (e.g. vendorTaxNumber)
 */
@Target(AnnotationTarget.CLASS)
annotation class Tenant(
        val buyer: String,
        val seller: String
)