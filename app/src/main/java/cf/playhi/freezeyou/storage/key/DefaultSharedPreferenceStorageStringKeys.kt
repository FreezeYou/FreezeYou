package cf.playhi.freezeyou.storage.key

import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_ADVANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_APPEARANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_COMMON

/**
 * The entry name must be the same as the key value in the preference xml.
 */
enum class DefaultSharedPreferenceStorageStringKeys {

    organizationName {
        override fun defaultValue(): String? = null
        override fun valueStringId(): Int = R.string.app_name
        override fun titleTextStringId(): Int = R.string.organizationName
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_ADVANCE
    },

    mainActivityPattern {
        override fun defaultValue() = "default"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.mainActivityPattern
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_APPEARANCE
    },

    uiStyleSelection {
        override fun defaultValue() = "default"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.uiStyle
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_APPEARANCE
    },

    themeOfAutoSwitchDarkMode {
        override fun defaultValue() = "dark"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.themeOfAutoSwitchDarkMode
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_APPEARANCE
    },

    launchMode {
        override fun defaultValue() = "all"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.launchMode
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    };

    /**
     * If null, use `stringId()` to get the String value.
     */
    abstract fun defaultValue(): String?

    /**
     * When `defaultValue()` is null, use the value of `stringId()` to get the String value.
     * If `defaultValue()` is not null, this will return -1.
     */
    abstract fun valueStringId(): Int

    abstract fun titleTextStringId(): Int
    abstract fun category(): Int
}
