package th.co.dv.p2p.common.utilities.search

/**
 * @author chatchch
 *
 */
class OrderBy {

    private var field: String? = null
    private var asc: Boolean = false

    constructor(field: String, asc: String) {
        OrderBy(field, "asc".equals(asc, ignoreCase = true))
    }

    constructor(field: String, asc: Boolean) {
        this.field = field
        this.asc = asc
    }

    /**
     * @return the field
     */
    fun getField(): String? {
        return field
    }

    /**
     * @param field the field to set
     */
    fun setField(field: String) {
        this.field = field
    }

    /**
     * @return the asc
     */
    fun isAsc(): Boolean {
        return asc
    }

    /**
     * @param asc the asc to set
     */
    fun setAsc(asc: Boolean) {
        this.asc = asc
    }

    fun createQuery(): String {
        return createQuery(null)
    }

    fun createQuery(prefix: String?): String {
        val sb = StringBuilder()
        sb.append(" ")
        if (null != prefix) {
            sb.append("$prefix.")
        }
        sb.append(field)
        if (asc) {
            sb.append(" ASC")
        } else {
            sb.append(" DESC")
        }

        return sb.toString()
    }
}