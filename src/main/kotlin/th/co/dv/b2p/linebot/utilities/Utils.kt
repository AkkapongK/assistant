package th.co.dv.b2p.linebot.utilities

import com.google.common.base.Enums

object Utils {

    /**
     *
     * Gets the enum for the class, returning `null` if not found.
     *
     *
     * This method differs from Enum.valueOf in that it does not throw an exception
     * for an invalid enum name and performs case insensitive matching of the name.
     *
     * @param <E>         the type of the enumeration
     * @param enumName    the enum name, null returns null
     * @return the enum, null if not found
     * @since 3.8
    </E> */
    inline fun <reified E : Enum<E>> getEnumIgnoreCase(enumName: String?): E? {
        if (enumName == null || !E::class.java.isEnum) {
            return null
        }
        for (each in E::class.java.enumConstants) {
            if (each.name.equals(enumName, ignoreCase = true)) {
                return each
            }
        }
        return null
    }
}