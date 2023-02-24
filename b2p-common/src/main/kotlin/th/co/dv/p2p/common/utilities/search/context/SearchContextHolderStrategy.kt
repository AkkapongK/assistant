package th.co.dv.p2p.common.utilities.search.context

/**
 * A strategy for storing Search context information against a thread.
 * <p>
 * The preferred strategy is loaded by
 * {@link th.co.dv.p2p.corda.base.utility.search.context.ConfigurationHolder.context.SearchContextHolder}
 * .
 * </p>
 *
 * @author Ben Alex
 * @version $Id: SearchContextHolderStrategy.java,v 1.1 2017/11/08 03:15:40 wittawai Exp $
 */
interface SearchContextHolderStrategy {
    // ~ Methods
    // ========================================================================================================

    /**
     * Clears the current context.
     */
    fun clearContext()

    /**
     * Obtains the current context.
     *
     * @return a context (never `null` - create a default
     * implementation if necessary)
     */
    fun getContext(): SearchContext

    /**
     * Sets the current context.
     *
     * @param context to the new argument (should never be `null`,
     * although implementations must check if `null` has been
     * passed and throw an `IllegalArgumentException` in such
     * cases)
     */
    fun setContext(context: SearchContext?)
}