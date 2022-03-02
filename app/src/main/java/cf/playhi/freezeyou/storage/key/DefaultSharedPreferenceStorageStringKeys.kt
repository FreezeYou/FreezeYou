package cf.playhi.freezeyou.storage.key

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_ADVANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_APPEARANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_COMMON

/**
 * The entry name must be the same as the key value in the preference xml.
 */
enum class DefaultSharedPreferenceStorageStringKeys : AbstractKey<String?> {

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
    abstract override fun defaultValue(): String?

    /**
     * When `defaultValue()` is null, use the value of `stringId()` to get the String value.
     * If `defaultValue()` is not null, this will return -1.
     */
    @StringRes
    abstract fun valueStringId(): Int

    @StringRes
    abstract override fun titleTextStringId(): Int
    abstract override fun category(): Int
    override fun getValue(context: Context?): String? {
        if (context == null)
            throw RuntimeException("Context cannot be null when getting keys from sharedPreferences.")

        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                this.name,
                if (this.defaultValue() == null) context.getString(this.valueStringId())
                else this.defaultValue()
            )
    }

    override fun setValue(context: Context?, value: String?) {
        if (context == null)
            throw RuntimeException("Context cannot be null when getting keys from sharedPreferences.")

        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(this.name, value).apply()
    }
}
