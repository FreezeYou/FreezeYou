# Target SDK 37 migration plan

| Field | Value |
|---|---|
| Date | 2026-08-09 |
| Current target | Android 13 / API 33 |
| Planned target | Android 17 / API 37 |
| Minimum SDK | API 23, unchanged |
| Compile SDK | API 37, already configured |
| Build tools | 37.0.0, already configured |
| AGP | 9.3.1, already sufficient |
| Status | Ready for implementation |

## Decision

Migrate to API 37, the newest official target SDK. Advance `targetSdk` one API level at a time during implementation so failures can be attributed to one platform release, then leave the committed result at 37.

API 37 has reached Platform Stability, its API surface is final, and Google Play publishing is open. Google Play requires phone app updates to target at least API 36 beginning 2026-08-31, so API 36 is the release fallback only if an API 37 runtime or dependency blocker cannot be resolved in time. It is not the desired endpoint.

Do not change `minSdk = 23`. `compileSdk = 37`, Build Tools 37, AGP 9.3.1, Kotlin 2.4.0, and the installed SDK platforms are already adequate.

## Executive risk summary

| Priority | Target gate | Repository impact | Required outcome |
|---|---|---|---|
| P0 | API 34 | Seven foreground services have no type | Declare correct types and permissions; add timeout cleanup |
| P0 | API 34 | Two Quick Settings tiles use the failing `Intent` overload | Use explicit immutable activity `PendingIntent` on API 34+ |
| P0 | API 35 | Edge-to-edge is enforced, but screens do not consume system-bar/IME insets | Establish one inset policy and audit every Activity class |
| P0 | API 36 | Scheduled-task editor intercepts `KEYCODE_BACK` | Migrate to AndroidX `OnBackPressedDispatcher` |
| P0 | API 37 | ReLinker fallback loads a writable extracted native library | Remove ReLinker and use MMKV's normal loader |
| P1 | API 34+ | Hidden APIs are central to freeze/mobile-data features | Exercise Shizuku/root fallbacks on each new OS |
| P1 | API 34 | Persistent services need `specialUse` | Document subtypes and complete Play Console FGS declaration |
| P1 | API 37 | No Android 37 system image is installed locally | Add an API 37 AVD or equivalent device-lab run before release |

Current positive findings:

- Runtime receiver export flags, package-scoped broadcasts, notification permission handling, and PackageInstaller callback explicitness are already compatible.
- Exact-alarm denial already degrades to inexact scheduling and diagnosis links to the grant screen.
- There is no local-network socket/discovery code, contacts/SMS access, background audio, `BluetoothSocket`, `MessageQueue` reflection, or app code that mutates static final fields.
- The current debug APK passes `zipalign -c -P 16 -v 4`; arm64 MMKV and AndroidX graphics-path `PT_LOAD` segments are aligned to `0x4000` (16 KiB).

## Phase 0 — Freeze a target-33 baseline

1. Record a clean baseline from the current commit:
   - `:app:compileDebugKotlin --warning-mode all`
   - `:app:lintDebug --warning-mode all`
   - `:app:assembleDebug`
   - `:app:assembleRelease`
2. Save cold-start, crash-buffer, and key-flow results from the existing API 34 AVD.
3. Add small instrumentation tests where practical because the repository currently has no source tests:
   - Activity cold launch and recreation.
   - Scheduled-task editor Back handling.
   - MMKV initialization/read/write in the main and `:backgroundService` processes.
4. Keep each target-level change independently reviewable. Do not combine unrelated UI redesigns or dependency upgrades.

Exit gate: target-33 behavior and build output are reproducible before compatibility changes begin.

## Phase 1 — Target API 34

### 1.1 Declare foreground-service types

Update `app/src/main/AndroidManifest.xml` and add the type-specific permissions.

| Service | Proposed type | Reason |
|---|---|---|
| `.service.InstallPackagesService` | `dataSync` | Reads/copies APK data and performs install/uninstall work |
| `.service.FUFService` | `shortService` | One-shot freeze/unfreeze operation |
| `.service.ForceStopService` | `shortService` | One-shot force-stop operation |
| `.service.OneKeyFreezeService` | `shortService` | User-triggered batch operation that stops itself |
| `.service.OneKeyUFService` | `shortService` | User-triggered batch operation that stops itself |
| `.service.ScreenLockOneKeyFreezeService` | `specialUse` | Persistent screen-state observer with no standard FGS category |
| `.TriggerTasksService` | `specialUse` | Persistent screen-trigger task observer with no standard FGS category |

Manifest work:

- Add `android.permission.FOREGROUND_SERVICE_DATA_SYNC`.
- Add `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`.
- Set `android:foregroundServiceType` on all seven services.
- Add an `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` property under each `specialUse` service with a precise, user-visible explanation.
- Prepare matching Play Console foreground-service declarations and demo instructions for both `specialUse` cases.

Service work:

- Add safe cleanup for both `Service.onTimeout(startId)` and `Service.onTimeout(startId, fgsType)` in `FreezeYouBaseService`, delegating to service-specific cancellation where necessary.
- Ensure one-shot services stop foreground state and call `stopSelf(startId)` on success, failure, and null-intent paths.
- Measure large one-key lists. A `shortService` must remain below the platform timeout; if it cannot, redesign that operation rather than disguising it as `specialUse`.
- Implement `InstallPackagesService` timeout cleanup because `dataSync` receives a target-35 runtime limit in the next phase.

### 1.2 Fix Quick Settings tile launches

Update:

- `LaunchFreezeYouQSTileService.kt`
- `OneKeyScreenLockQSTileService.kt`

On API 34+, call `TileService.startActivityAndCollapse(PendingIntent)` using an explicit immutable `PendingIntent.getActivity`. Retain the deprecated `Intent` overload only below API 34.

### 1.3 Validate hidden-platform integrations

No generic code patch can guarantee these private APIs:

- `FUFSinglePackage.kt`: `android.content.pm.IPackageManager$Stub` reflection.
- `TasksUtils.kt`: hidden mobile-data setters on `ConnectivityManager` and `TelephonyManager`.

Test device-owner, Shizuku, and root modes. Preserve a working fallback and a clear failure result when a hidden call is blocked.

### 1.4 Set and verify target 34

Set `targetSdk = 34`, build, and test:

- Fresh install and upgrade from target 33.
- Every foreground service from UI, notification, tile, exact alarm, boot, accessibility, and screen-on/off entry points.
- Both activity-opening tiles while locked and unlocked.
- Exact tasks with permission denied and granted.
- PackageInstaller install/uninstall callbacks.
- Large freeze/unfreeze/force-stop lists below the short-service timeout.
- Persistent observers across reboot, process recreation, and screen transitions.
- Crash/ANR buffers for `MissingForegroundServiceTypeException`, `SecurityException`, `ForegroundServiceStartNotAllowedException`, and service timeouts.

Exit gate: no missing FGS type/permission errors, tile launches work, and all privileged modes have a known result.

## Phase 2 — Target API 35

### 2.1 Implement edge-to-edge deliberately

API 35 enforces edge-to-edge on Android 15. The repository currently has transparent system-bar styling but no consistent `WindowInsets` consumption, so changing only `targetSdk` will place content and controls beneath system bars.

Implementation policy:

1. Call AndroidX `enableEdgeToEdge()` from `FreezeYouBaseActivity.onCreate()` so behavior is consistent across supported OS versions.
2. Add a shared Activity-root inset contract instead of padding individual widgets ad hoc:
   - Compose screen roots consume `WindowInsets.safeDrawing` exactly once.
   - Search/forms additionally handle IME insets without double-padding.
   - View/Fragment roots use `ViewCompat.setOnApplyWindowInsetsListener` at their outer owner.
3. Give these special windows an explicit policy rather than the normal screen padding:
   - dialog/translucent Activities;
   - `FullScreenImageViewerActivity`;
   - AppLock;
   - shortcut folder/dialog presentation.
4. Reconcile or remove obsolete assumptions in `ThemeUtils.processAddTranslucent()` and `values-v21`/`values-v23` system-bar colors. Verify light/dark system-bar icon contrast.

Audit every Activity, with priority on:

- Main search/list/grid and bottom-end floating button.
- Scheduled-task list and editor floating buttons.
- Settings/Preference fragment hosts.
- AppLock, dialogs, backup/import, icon picker, package lists, log viewer, folder, and full-screen image.

### 2.2 Complete target-35 service behavior

- Verify `InstallPackagesService` stops promptly when its cumulative `dataSync` allowance expires and leaves queued work in a recoverable state.
- Confirm `BOOT_COMPLETED` starts only the proposed `specialUse` observer; do not start a `dataSync` service from boot.
- Exercise notification `PendingIntent` activity launches. They are explicit and user-initiated, so no background-activity-launch opt-in is expected.
- The app does not declare `SYSTEM_ALERT_WINDOW`, use media projection, modify DND, or use restricted TLS/network clients; no patch is planned for those changes.

### 2.3 Confirm 16 KiB page-size compliance

The existing checks pass, but repeat them on the final release artifact and all delivered ABIs:

- Build the release APK/AAB.
- Run `zipalign -c -P 16 -v 4` on generated APKs.
- Run the official ELF alignment check on every packaged `.so`.
- Cold-start on an Android 15+ 16 KiB image with compatibility mode disabled.
- Exercise MMKV in the main and background processes.

### 2.4 Set and verify target 35

Set `targetSdk = 35` and test on an API 35 device in gesture and three-button navigation:

- Portrait/landscape, display cutout, light/dark themes.
- First/last list rows and all bottom controls remain reachable.
- Main search and editors with the IME visible.
- Dialog/translucent/full-screen windows preserve their intended appearance.
- All API 34 service, tile, exact-alarm, install, root, and Shizuku tests remain green.

Exit gate: no content is obscured or double-inset and the release artifact is fully 16 KiB compatible.

## Phase 3 — Target API 36

### 3.1 Migrate custom Back handling

`ScheduledTasksAddActivity.kt` uses `onKeyDown(KEYCODE_BACK)` to invoke `checkAndDecideIfFinish()`. Target 36 on Android 16 no longer dispatches that legacy path.

- Remove the `KeyEvent` override.
- Register an AndroidX `OnBackPressedCallback` with `onBackPressedDispatcher`.
- Keep the callback enabled only while this Activity owns Back.
- Preserve the exact save/discard/cancel behavior and avoid invoking the dialog twice during predictive-Back commit.

Do not use `android:enableOnBackInvokedCallback="false"` as a migration shortcut.

### 3.2 Treat API-35 inset work as a hard prerequisite

API 36 removes the edge-to-edge opt-out on Android 16. Do not advance the target until the Phase 2 screen matrix passes.

### 3.3 Validate adaptive layouts

The app declares no orientation, aspect-ratio, or resizability restrictions, so no manifest patch is expected. Test API 36 at `sw600dp` because the platform ignores such restrictions for apps that do have them and exercises resizing more broadly:

- Phone portrait/landscape.
- Tablet/foldable portrait/landscape.
- Split-screen/freeform resize loops.
- Main list/grid, Settings fragments, shortcut editor/folder, scheduled-task editor, dialogs, and state retention.

Do not add the temporary restricted-resizability opt-out; it stops working at target 37.

### 3.4 Set and verify target 36

Set `targetSdk = 36` and verify:

- Gesture and three-button Back in edited and unedited scheduled tasks.
- Save, discard, and cancel branches appear exactly once.
- AndroidX-managed Back navigation across the rest of the app.
- Exact alarms after grant, revoke, reboot, and app upgrade.
- 16 KiB native cold start and MMKV read/write.
- Full API 34/35 regression suite.

Exit gate: predictive Back cannot bypass unsaved-work handling, and phone/tablet layouts are usable under mandatory edge-to-edge.

## Phase 4 — Target API 37

### 4.1 Remove the incompatible ReLinker fallback

`MainApplication.kt` passes a ReLinker callback to `MMKV.initialize()`, and `app/build.gradle` depends on ReLinker 1.4.5. Its extractor explicitly leaves the copied library writable before calling `System.load()`. Android 17 rejects that path for target-37 apps with `UnsatisfiedLinkError`.

Preferred migration:

- Remove `com.getkeepsafe.relinker:relinker`.
- Replace the custom loader callback with normal `MMKV.initialize(this)`.
- Do not add another writable extracted-native fallback.

This is supported by ReLinker's own guidance: the loader bugs it works around were resolved by Marshmallow, and the app's `minSdk` is already 23.

MMKV acceptance tests:

- Fresh install and upgrade with existing MMKV files.
- Main-process and `:backgroundService` initialization.
- `DefaultMultiProcessMMKVStorage`, average-time storage, AppLock ashmem storage, and migration locks.
- Force-stop/relaunch, reboot, backup/import, and process concurrency.
- Confirm no `UnsatisfiedLinkError` and no data reset.

### 4.2 Audit remaining Android 17 changes

- Local-network permission: not applicable; no LAN socket/discovery/network client code was found. Do not request `ACCESS_LOCAL_NETWORK`.
- MessageQueue reflection and static-final mutation: not present in app code; retain dependency smoke tests.
- Contacts, SMS OTP, background audio, and `BluetoothSocket` changes: not applicable.
- Certificate transparency/ECH: no app networking stack was found.
- Large-screen restriction opt-outs are unavailable; Phase 3 adaptive tests remain mandatory.
- Re-run StrictMode unsafe-intent checks and exported-entry integration tests, but do not weaken Android 17 activity-security defaults globally.

### 4.3 Set and verify target 37

Set `targetSdk = 37`, then:

- Install an official API 37 system image or use a device lab; the local SDK currently has platforms/build tools but no API 37 runtime image.
- Repeat fresh-install and upgrade tests from the last target-33 release.
- Run all core flows on API 37 with crash/ANR/native-linker logs captured.
- Test 16 KiB mode with compatibility fallback disabled/fatal where available.
- Repeat phone, tablet, gesture, three-button, cutout, IME, dark-theme, foreground-service, alarm, notification, tile, install/uninstall, device-owner, Shizuku, and root scenarios.

Exit gate: API 37 cold start and multiprocess MMKV work without the ReLinker fallback, with no target-SDK compatibility exceptions.

## Final build and release gate

Required automated commands:

```text
./gradlew :app:compileDebugKotlin --warning-mode all
./gradlew :app:lintDebug --warning-mode all
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

Required artifact checks:

- Merged manifest reports `targetSdkVersion=37` and the expected FGS types/permissions/properties.
- Debug and minified release builds cold-start.
- R8/resource shrinking completes without missing reflected components.
- APK/AAB-derived APKs pass 16 KiB ZIP and ELF alignment.
- No new lint fatal/error findings are suppressed merely to finish the bump.
- `git diff --check` is clean.

Minimum runtime matrix:

| Runtime | Purpose |
|---|---|
| API 23 | Minimum-SDK and normal MMKV loading |
| API 34 | FGS types, tiles, exact alarms, hidden APIs |
| API 35 | Edge-to-edge, IME/system bars, 16 KiB |
| API 36 | Predictive Back and adaptive layouts |
| API 37 | Native DCL, final target behavior, security smoke tests |

Release only after the API 37 row passes. If an external dependency blocks API 37 close to the Play deadline, ship the already-validated API 36 stage and continue Phase 4 immediately afterward.

## Files expected to change

- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/cf/playhi/freezeyou/MainApplication.kt`
- `app/src/main/java/cf/playhi/freezeyou/app/FreezeYouBaseActivity.kt`
- `app/src/main/java/cf/playhi/freezeyou/app/FreezeYouBaseService.kt`
- `app/src/main/java/cf/playhi/freezeyou/LaunchFreezeYouQSTileService.kt`
- `app/src/main/java/cf/playhi/freezeyou/OneKeyScreenLockQSTileService.kt`
- `app/src/main/java/cf/playhi/freezeyou/ui/ScheduledTasksAddActivity.kt`
- Compose Activity roots and the small set of View/Fragment hosts requiring explicit inset ownership
- Tests added under `app/src/androidTest/` and/or `app/src/test/`

## Official references

- [Android 14 target behavior changes](https://developer.android.com/about/versions/14/behavior-changes-14)
- [Foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types)
- [Declaring foreground services](https://developer.android.com/develop/background-work/services/fgs/declare)
- [Android 14 non-SDK restrictions](https://developer.android.com/about/versions/14/changes/non-sdk-14)
- [Android 15 target behavior changes](https://developer.android.com/about/versions/15/behavior-changes-15)
- [Android edge-to-edge guidance](https://developer.android.com/develop/ui/compose/system/setup-e2e)
- [16 KiB page-size support](https://developer.android.com/guide/practices/page-sizes)
- [Android 16 target behavior changes](https://developer.android.com/about/versions/16/behavior-changes-16)
- [Predictive Back custom navigation](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)
- [Android 17 target behavior changes](https://developer.android.com/about/versions/17/behavior-changes-17)
- [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk)
- [Android 17 release notes / Platform Stability](https://developer.android.com/about/versions/17/release-notes)
- [Google Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- [ReLinker project guidance](https://github.com/KeepSafe/ReLinker)
- [MMKV Android loader guidance](https://github.com/Tencent/MMKV/wiki/android_advance)
