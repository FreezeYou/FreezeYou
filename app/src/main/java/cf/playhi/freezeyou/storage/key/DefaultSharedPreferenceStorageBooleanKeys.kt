package cf.playhi.freezeyou.storage.key

import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_APPEARANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_COMMON
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_ICON_ENTRY
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_INSTALL_UNINSTALL

enum class DefaultSharedPreferenceStorageBooleanKeys {

    allowFollowSystemAutoSwitchDarkMode {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.allowFollowSystemAutoSwitchDarkMode
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_APPEARANCE
    },

    allowEditWhenCreateShortcut {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.allowEditWhCreateShortcut
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    noCaution {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.nSCaution
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    saveOnClickFunctionStatus {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.saveOnClickFunctionStatus
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    saveSortMethodStatus {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.saveSortMethodStatus
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    cacheApplicationsIcons {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.cacheApplicationsIcons
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    showInRecents {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.showInRecents
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    shortcutAutoFUF {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.shortcutAutoFUF
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    needConfirmWhenFreezeUseShortcutAutoFUF {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.needCfmWhenFreeze
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    openImmediatelyAfterUnfreezeUseShortcutAutoFUF {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.openImmediatelyAfterUF
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    firstIconEnabled {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = -1
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_ICON_ENTRY
    },

    secondIconEnabled {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = -1
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_ICON_ENTRY
    },

    thirdIconEnabled {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = -1
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_ICON_ENTRY
    },

    enableInstallPkgFunc {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.enableInstallPkgFunc
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_INSTALL_UNINSTALL
    };

    abstract fun defaultValue(): Boolean
    abstract fun titleTextStringId(): Int
    abstract fun category(): Int
}