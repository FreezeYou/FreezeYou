package cf.playhi.freezeyou.storage.key

import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_APPEARANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE

enum class DefaultMultiProcessMMKVStorageStringKeys {

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
    abstract fun defaultValue(): String?

    /**
     * When `defaultValue()` is null, use the value of `stringId()` to get the String value.
     * If `defaultValue()` is not null, this will return -1.
     */
    abstract fun valueStringId(): Int

    abstract fun titleTextStringId(): Int
    abstract fun category(): Int
}