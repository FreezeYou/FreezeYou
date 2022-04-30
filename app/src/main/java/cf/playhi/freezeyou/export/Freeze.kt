package cf.playhi.freezeyou.export

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import cf.playhi.freezeyou.fuf.FUFSinglePackage
import cf.playhi.freezeyou.fuf.FUFSinglePackage.Companion.ERROR_OTHER
import cf.playhi.freezeyou.utils.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Freeze : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val context = context
        val bundle = Bundle()
        if (method == null || extras == null) {
            return bundle
        }
        val pkgName = extras.getString("packageName")
        if (context == null) {
            bundle.putInt("result", -1)
            return bundle
        }
        if (pkgName == null) {
            bundle.putInt("result", -2)
            return bundle
        }
        return when (method) {
            @Suppress("DEPRECATION")
            FUFMode.MODE_AUTO -> {
                if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                    bundle.putInt("result", 998)
                } else {
                    if (Build.VERSION.SDK_INT >= 21 && DevicePolicyManagerUtils.isDeviceOwner(
                            context
                        )
                    ) {
                        if (FUFUtils.checkMRootFrozen(context, pkgName)) {
                            bundle.putInt("result", 999)
                        } else {
                            if (FUFUtils.processMRootAction(
                                    context, pkgName,
                                    null, null, hidden = true,
                                    askRun = false, runImmediately = false, activity = null,
                                    finish = false, showUnnecessaryToast = false
                                )
                            ) {
                                bundle.putInt("result", 0)
                            } else {
                                bundle.putInt("result", -3)
                            }
                        }
                    } else if (!FUFUtils.checkRootFrozen(context, pkgName, null)) {
                        if (FUFUtils.processRootAction(
                                pkgName, null, null,
                                context, enable = false, askRun = false, runImmediately = false,
                                activity = null, finish = false, showUnnecessaryToast = false
                            )
                        ) {
                            bundle.putInt("result", 0)
                        } else {
                            bundle.putInt("result", -4)
                        }
                    } else {
                        bundle.putInt("result", 999)
                    }
                }
                bundle
            }
            @Suppress("DEPRECATION")
            FUFMode.MODE_MROOT -> {
                if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                    bundle.putInt("result", 998)
                } else {
                    if (FUFUtils.checkMRootFrozen(context, pkgName)) {
                        bundle.putInt("result", 999)
                    } else {
                        if (FUFUtils.processMRootAction(
                                context, pkgName, null,
                                null, hidden = true, askRun = false,
                                runImmediately = false, activity = null,
                                finish = false, showUnnecessaryToast = false
                            )
                        ) {
                            bundle.putInt("result", 0)
                        } else {
                            bundle.putInt("result", -3)
                        }
                    }
                }
                bundle
            }
            @Suppress("DEPRECATION")
            FUFMode.MODE_ROOT -> {
                if (ApplicationInfoUtils.getApplicationInfoFromPkgName(pkgName, context) == null) {
                    bundle.putInt("result", 998)
                } else {
                    if (FUFUtils.checkRootFrozen(context, pkgName, null)) {
                        bundle.putInt("result", 999)
                    } else {
                        if (FUFUtils.processRootAction(
                                pkgName, null, null,
                                context, enable = false, askRun = false, runImmediately = false,
                                activity = null, finish = false, showUnnecessaryToast = false
                            )
                        ) {
                            bundle.putInt("result", 0)
                        } else {
                            bundle.putInt("result", -4)
                        }
                    }
                }
                bundle.putInt("result", 0)
                bundle
            }
            FUFMode.MODE_DPM -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_MROOT_DPM
                    )
                )
                bundle
            }
            FUFMode.MODE_ROOT_DISABLE_ENABLE -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_ROOT_DISABLE_ENABLE
                    )
                )
                bundle
            }
            FUFMode.MODE_ROOT_HIDE_UNHIDE -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_ROOT_UNHIDE_HIDE
                    )
                )
                bundle
            }
            @Suppress("DEPRECATION")
            FUFMode.MODE_LEGACY_AUTO -> {
                bundle.putInt(
                    "result",
                    @Suppress("DEPRECATION")
                    doApiV2Action(
                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_LEGACY_AUTO
                    )
                )
                bundle
            }
            FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName, FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE
                    )
                )
                bundle
            }
            FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_USER -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName,
                        FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_USER
                    )
                )
                bundle
            }
            FUFMode.MODE_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName,
                        FUFSinglePackage.API_FREEZEYOU_SYSTEM_APP_ENABLE_DISABLE_UNTIL_USED
                    )
                )
                bundle
            }
            FUFMode.MODE_PROFILE_OWNER -> {
                bundle.putInt(
                    "result",
                    doApiV2Action(
                        context, pkgName,
                        FUFSinglePackage.API_FREEZEYOU_MROOT_PROFILE_OWNER
                    )
                )
                bundle
            }
            else -> {
                bundle.putInt("result", FUFSinglePackage.ERROR_NO_SUCH_API_MODE)
                bundle
            }
        }
    }

    private fun doApiV2Action(context: Context, pkgName: String, apiMode: Int): Int {
        return runBlocking {
            var result = ERROR_OTHER
            launch {
                result =
                    FUFSinglePackage(context, pkgName, FUFSinglePackage.ACTION_MODE_FREEZE, apiMode)
                        .commit()
                if (FUFUtils.preProcessFUFResultAndShowToastAndReturnIfResultBelongsSuccess(
                        context, result, false
                    )
                ) {
                    FUFUtils.sendStatusChangedBroadcast(context)
                    TasksUtils.onFApplications(context, pkgName)
                    NotificationUtils.deleteNotification(context, pkgName)
                }
            }.join()
            return@runBlocking result
        }
    }

}
