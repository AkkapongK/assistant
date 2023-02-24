package th.co.dv.p2p.common.utilities.search.context

/**
 * A <code>ThreadLocal</code>-based implementation of
 * {@link th.co.dv.p2p.corda.base.utility.search.context.SearchContextHolderStrategy}
 * .
 *
 * @author Ben Alex
 * @version $Id: ThreadLocalSearchContextHolderStrategy.java,v 1.1 2017/11/08 03:15:40 wittawai Exp $
 * @see ThreadLocal
 */
class ThreadLocalSearchContextHolderStrategy: SearchContextHolderStrategy {

    // ~ Static fields/initializer
    // =====================================================================================

    private val contextHolder = ThreadLocal<SearchContext>()

    // ~ Methods
    // ========================================================================================================

    override fun clearContext() {
        contextHolder.set(null)
    }

    override fun getContext(): SearchContext {
        if (contextHolder.get() == null) {
            contextHolder.set(SearchContextImpl())
        }

        return contextHolder.get() as SearchContext
    }

    override fun setContext(context: SearchContext?) {

        if(context == null) {
            throw IllegalArgumentException("Only non-null SearchContext instances are permitted")
        }
        contextHolder.set(context)
    }
}