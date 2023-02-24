package th.co.dv.p2p.common.utilities.search.context

import java.lang.reflect.UndeclaredThrowableException

/**
 * Associates a given {@link SearchContext} with the current execution thread.
 * <p>
 * This class provides a series of static methods that delegate to an instance
 * of {@link org.springframework.security.context.SecurityContextHolderStrategy}
 * . The purpose of the class is to provide a convenient way to specify the
 * strategy that should be used for a given JVM. This is a JVM-wide setting,
 * since everything in this class is <code>static</code> to facilitate ease of
 * use in calling code.
 * </p>
 * <p>
 * To specify which strategy should be used, you must provide a mode setting. A
 * mode setting is one of the three valid <code>MODE_</code> settings defined as
 * <code>static final</code> fields, or a fully qualified classname to a
 * concrete implementation of
 * {@link org.springframework.security.context.SecurityContextHolderStrategy}
 * that provides a public no-argument constructor.
 * </p>
 * <p>
 * There are two ways to specify the desired strategy mode <code>String</code>.
 * The first is to specify it via the system property keyed on
 * {@link #SYSTEM_PROPERTY}. The second is to call
 * {@link #setStrategyName(String)} before using the class. If neither approach
 * is used, the class will default to using {@link #MODE_THREADLOCAL}, which is
 * backwards compatible, has fewer JVM incompatibilities and is appropriate on
 * servers (whereas {@link #MODE_GLOBAL} is definitely inappropriate for server
 * use).
 * </p>
 *
 * @author Ben Alex
 * @version $Id: SearchContextHolder.java,v 1.1 2017/11/08 03:15:40 wittawai Exp $
 * @see org.springframework.security.context.HttpSessionContextIntegrationFilter
 */
class SearchContextHolder {

    // ~ Static fields/initializers
    // =====================================================================================

    val MODE_THREADLOCAL = "MODE_THREADLOCAL"

    val MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL"

    val MODE_GLOBAL = "MODE_GLOBAL"

    val SYSTEM_PROPERTY = "search.strategy"

    private var strategyName: String? = System.getProperty(SYSTEM_PROPERTY)

    private var strategy: SearchContextHolderStrategy? = null

    private var initializeCount = 0

    init {
        initialize()
    }

    // ~ Methods
    // ========================================================================================================

    /**
     * Explicitly clears the context value from the current thread.
     */
    fun clearContext() {
        strategy!!.clearContext()
    }

    /**
     * Obtain the current `SearchContext`.
     *
     * @return the Search context (never `null`)
     */
    fun getContext(): SearchContext {
        return strategy!!.getContext()
    }

    /**
     * Primarily for troubleshooting purposes, this method shows how many times
     * the class has reinitialized its `SearchContextHolderStrategy`.
     *
     * @return the count (should be one unless you've called
     * [.setStrategyName] to switch to an alternate
     * strategy.
     */
    fun getInitializeCount(): Int {
        return initializeCount
    }

    private fun initialize() {
        if (strategyName == null || "" == strategyName) {
            // Set default
            strategyName = MODE_THREADLOCAL
        }

        strategy = if (strategyName == MODE_THREADLOCAL) {
            ThreadLocalSearchContextHolderStrategy()
        } else {
            // Try to load a custom strategy
            try {
                val clazz = Class.forName(strategyName)
                val customStrategy = clazz.getConstructor(*arrayOf())
                customStrategy.newInstance(*arrayOf()) as SearchContextHolderStrategy
            } catch (ex: Exception) {
                throw UndeclaredThrowableException(ex)
            }

        }

        initializeCount++
    }

    /**
     * Associates a new `SearchContext` with the current thread of
     * execution.
     *
     * @param context the new `SearchContext` (may not be
     * `null`)
     */
    fun setContext(context: SearchContext) {
        strategy!!.setContext(context)
    }

    /**
     * Changes the preferred strategy. Do *NOT* call this method more
     * than once for a given JVM, as it will reinitialize the strategy and
     * adversely affect any existing threads using the old strategy.
     *
     * @param strategyName the fully qualified classname of the strategy that
     * should be used.
     */
    fun setStrategyName(strategyName: String) {
        SearchContextHolder().strategyName = strategyName
        initialize()
    }

    override fun toString(): String {
        return "SearchContextHolder[strategy='$strategyName'; initializeCount=$initializeCount]"
    }
}