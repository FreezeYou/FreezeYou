package cf.playhi.freezeyou.storage.key

import android.content.Context
import androidx.annotation.StringRes
import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.datastore.DefaultMultiProcessMMKVDataStore
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_APPEARANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE

enum class DefaultMultiProcessMMKVStorageStringKeys : AbstractKey<String?> {

    languagePref {
        override fun defaultValue() = "Default"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.displayLanguage
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_APPEARANCE
    },

    selectFUFMode {
        override fun defaultValue() = "0"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.selectFUFMode
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    shortCutOneKeyFreezeAdditionalOptions {
        override fun defaultValue() = "nothing"
        override fun valueStringId(): Int = -1
        override fun titleTextStringId(): Int = R.string.shortCutOneKeyFreezeAdditionalOptions
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
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
        return DefaultMultiProcessMMKVDataStore().getString(
            this.name,
            if (this.defaultValue() == null) context?.getString(this.valueStringId())
            else this.defaultValue()
        )
    }

    override fun setValue(context: Context?, value: String?) {
        DefaultMultiProcessMMKVDataStore().putString(this.name, value)
    }
}