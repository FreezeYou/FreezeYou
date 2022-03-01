package cf.playhi.freezeyou.storage.key

import cf.playhi.freezeyou.R
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_ADVANCE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_AUTOMATION
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_BACKGROUND_SERVICE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_COMMON
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_INSTALL_UNINSTALL
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_NOTIFICATION
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_NOTIFICATION_FUF
import cf.playhi.freezeyou.storage.key.KeyCategory.CATEGORY_SETTINGS_SECURITY

enum class DefaultMultiProcessMMKVStorageBooleanKeys {

    debugModeEnabled {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.debugMode
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_ADVANCE
    },

    onekeyFreezeWhenLockScreen {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.freezeAfterScreenLock
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_AUTOMATION
    },

    freezeOnceQuit {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.freezeOnceQuit
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_AUTOMATION
    },

    useForegroundService {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.useForegroundService
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_BACKGROUND_SERVICE
    },

    lesserToast {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.lesserToast
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_COMMON
    },

    avoidFreezeForegroundApplications {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.avoidFreezeForegroundApplications
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    avoidFreezeNotifyingApplications {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.avoidFreezeNotifyingApplications
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    openImmediately {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.openImmediately
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    openAndUFImmediately {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.openAndUFImmediately
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_FREEZE_AND_UNFREEZE
    },

    tryDelApkAfterInstalled {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.tryDelApkAfterInstalled
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_INSTALL_UNINSTALL
    },

    notAllowInstallWhenIsObsd {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.notAllowWhenIsObsd
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_INSTALL_UNINSTALL
    },

    tryToAvoidUpdateWhenUsing {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.tryToAvoidUpdateWhenUsing
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_INSTALL_UNINSTALL
    },

    createQuickFUFNotiAfterUnfrozen {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.createQuickFUFNotiAfterUnfrozen
        override fun category(): Int =
            CATEGORY_SETTINGS or CATEGORY_SETTINGS_NOTIFICATION or CATEGORY_SETTINGS_NOTIFICATION_FUF
    },

    notificationBarFreezeImmediately {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.notificationBarFreezeImmediately
        override fun category(): Int =
            CATEGORY_SETTINGS or CATEGORY_SETTINGS_NOTIFICATION or CATEGORY_SETTINGS_NOTIFICATION_FUF
    },

    notificationBarDisableSlideOut {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.disableSlideOut
        override fun category(): Int =
            CATEGORY_SETTINGS or CATEGORY_SETTINGS_NOTIFICATION or CATEGORY_SETTINGS_NOTIFICATION_FUF
    },

    notificationBarDisableClickDisappear {
        override fun defaultValue(): Boolean = true
        override fun titleTextStringId(): Int = R.string.disableClickDisappear
        override fun category(): Int =
            CATEGORY_SETTINGS or CATEGORY_SETTINGS_NOTIFICATION or CATEGORY_SETTINGS_NOTIFICATION_FUF
    },

    enableAuthentication {
        override fun defaultValue(): Boolean = false
        override fun titleTextStringId(): Int = R.string.enableAuthentication
        override fun category(): Int = CATEGORY_SETTINGS or CATEGORY_SETTINGS_SECURITY
    };

    abstract fun defaultValue(): Boolean
    abstract fun titleTextStringId(): Int
    abstract fun category(): Int
}
