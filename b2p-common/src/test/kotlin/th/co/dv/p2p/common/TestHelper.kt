package th.co.dv.p2p.common

import th.co.dv.p2p.common.utilities.isNullOrEmpty
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

object TestHelper {
    inline fun <reified T, R> callMethod(obj: T, methodName: String, vararg args: Any?): R? {
        val method = getCallableMethod<R>(T::class, methodName)
        return try { method.apply { isAccessible = true }.call(obj, *args) } catch (e: Exception) { throw e.cause ?: e }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> getCallableMethod(kclazz: KClass<*>, methodName: String): KFunction<R> {
        val method = try {
            kclazz
                .declaredFunctions
                .first { it.name == methodName }
        } catch (e: Exception) {
            if (kclazz.superclasses.isNullOrEmpty().not()) {
                getCallableMethod<R>(kclazz.superclasses.first(), methodName)
            } else {
                throw e
            }
        }

        return method as KFunction<R>
    }
}


