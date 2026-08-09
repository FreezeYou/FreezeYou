# Review Report: `c991de57` — Migrate legacy XML layouts to Jetpack Compose

| Field | Value |
|-------|-------|
| **Commit** | `c991de570831291be74fc4549ca405922c46f24f` |
| **Author** | Playhi (Co-Authored-By: GPT-5.6 Sol) |
| **Date** | 2026-08-09 |
| **Scope** | 80 files, +2259 / −4023 |
| **Verdict** | Original commit: **not merge-ready**. Remediation working tree: **ready for review** |
| **Review date** | 2026-08-09 |
| **Remediation date** | 2026-08-09 |

---

## Summary

This commit replaces nearly all legacy XML activity/list/dialog layouts with Jetpack Compose (Material3), adds `ui/compose/*` helpers, and wires the Compose Gradle plugin + BOM. A few intentional fixes are real (main-list `masterAppList` clone for search, drawable-safe rendering, diagnosis LazyColumn crash avoided by omitting keys).

Overall the migration is incomplete and fragile:

- **Correctness / crashes**: dialog `ComposeView` without lifecycle owners, API-23 `locales` access, fragment transactions inside Compose `AndroidView`.
- **Main-path performance**: `ComposeView` cells inside `ListView`/`GridView` with per-bind bitmap rasterization.
- **Unexpected UX/UI**: AppCompat → Material3 visual language, lost animations, lost control styling, different menus/FABs, missing marquee/fast-scroll/selection chrome, theme surfaces not mapped from existing app themes.

Dominant risk is **runtime breakage + user-visible parity loss**, not pure style.

## Remediation update

The findings were re-audited against both `c991de57` and its parent. All 25 cited code patterns were present, with these qualifications:

- Issue 1 was a credible lifecycle weakness, although the reported crash did not reproduce with the tested AppCompat dialog. Explicit lifecycle, saved-state, and ViewModel-store owners are now assigned.
- Issue 14 was already a non-functional, unreachable stub in the parent implementation. It is not a Compose-migration regression and was intentionally left unchanged rather than introducing arbitrary command execution.
- The About-screen version header called out in the UX section was not a regression: the parent also rendered the version code.

The working tree resolves the other 24 actionable findings. It also restores the reviewed main-list performance characteristics, anchored menus, state restoration, scheduled-task behavior, theme mapping, accessibility, typography, marquee, selection styling, and divider behavior. The remediation is not yet committed.

### Verification completed

| Check | Result |
|-------|--------|
| `:app:compileDebugKotlin --warning-mode all` | passed |
| `:app:lintDebug --warning-mode all` | passed; no new API-23, Compose locale, lifecycle, or modified-adapter findings |
| `:app:assembleDebug` | passed |
| `:app:assembleRelease` | passed, including lint vital, R8, and resource shrinking |
| `git diff --check` | clean |
| API 34 AVD cold launch | passed; process remained alive and crash buffer was empty |
| Main list/grid, scrolling, popup/About navigation, and search restoration after recreation | passed |
| URI dialog, auto diagnosis, scheduled-task preference host, first-time setup, icon picker, and folder shortcut | smoke-tested without a crash |

Still recommended before release: an API 23 runtime pass, biometric AppLock testing, an eligible-caller always-allow persistence test, trigger-task enable/delete testing, and a real backup subset import.

---

## Issue counts

| Severity | Count |
|----------|------:|
| bug | 14 |
| suggestion | 9 |
| nit | 2 |
| **UX/UI unexpected changes** | many (section below; not all severity-tagged) |

---

## Issues (correctness / reliability)

### Issue 1 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/compose/DialogContent.kt:19`
- **Description:** `alwaysAllowView()` builds a bare `ComposeView` used as `AlertDialog.setView(...)` in `UriFreezeActivity` and `InstallPackagesActivity`. Dialog windows often lack `ViewTreeLifecycleOwner` / saved-state / ViewModel owners. That commonly crashes with “ViewTreeLifecycleOwner not found from ComposeView”, or leaves composition inert so the always-allow checkbox never reports state.
- **Suggestion:** Set owners from the host Activity before `setContent` (`setViewTreeLifecycleOwner`, `setViewTreeSavedStateRegistryOwner`, `setViewTreeViewModelStoreOwner`), or replace with a plain `CheckBox` / host in a DialogFragment that already has a lifecycle.
- **Status:** resolved — explicit view-tree owners are assigned and the entire checkbox row toggles.

### Issue 2 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/FUFLauncherShortcutCreator.kt:66`
- **Description:** `LocalConfiguration.current.locales[0]` uses `Configuration.getLocales()`, which is API 24+. `minSdk` is 23, so API-23 devices can crash the first time this screen composes.
- **Suggestion:** Use `Locale.getDefault()`, `ConfigurationCompat.getLocales(configuration)[0]`, or gate with `Build.VERSION.SDK_INT >= 24` and fall back to `configuration.locale`.
- **Status:** resolved — locale lookup now uses `ConfigurationCompat` with an API-23-safe fallback.

### Issue 3 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/Main.kt:217`
- **Description:** Main app list fragment is hosted by creating a `FragmentContainerView` inside Compose `AndroidView` and committing with `commitNowAllowingStateLoss()` from the composition `update` block. Fragment transactions during composition/recompositions are racy: config change / process restore can leave FM-restored fragments vs a newly constructed `MainActivityAppListFragment`, empty containers, or “No view found for id” / double-add failures. State loss is explicitly allowed.
- **Suggestion:** Prefer a stable XML/`FragmentContainerView` host, or commit once from a lifecycle callback after the container is attached—never from recomposition `update` with `AllowingStateLoss`. Reuse FM-restored fragments by tag/id.
- **Status:** resolved — the restored fragment is reused and attached once after the container is attached, using a normal transaction without state loss.

### Issue 4 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/adapter/MainAppListSimpleAdapter.kt:50`
- **Description:** Every list/grid row is a recycled `ComposeView` that calls `setContent { ... }` on every `getView`. Combined with `DrawableImage` → `getBitmapFromDrawable` (rasterizes non-`BitmapDrawable` icons) and `bitmap.asImageBitmap()` without caching, scrolling the main app list will recompose and re-allocate ImageBitmaps constantly. High risk of jank/ANR on real devices with large package sets—the primary screen of the app.
- **Suggestion:** Prefer Compose `LazyColumn`/`LazyVerticalGrid` for the main list, or keep classic `ImageView` binders. If keeping `ComposeView` cells: set `AbsListView.LayoutParams`, avoid full `setContent` when position/item unchanged, and cache `ImageBitmap` / use Coil/AsyncImage for icons.
- **Status:** resolved — main rows use lightweight native recycled views and drawables instead of a `ComposeView` and bitmap conversion per bind.

### Issue 5 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/SelectShortcutIconActivity.kt:72`
- **Description:** `init()` enumerates every installed application and loads icons synchronously on the main thread before `setContent`. On devices with hundreds of apps this blocks first frame and can ANR.
- **Suggestion:** Load icons off the main thread (or lazily per visible cell), show a progress indicator, then publish a state list—mirror `FUFLauncherShortcutCreator.loadApplications()`.
- **Status:** resolved — application/icon enumeration runs off the main thread with a loading state.

### Issue 6 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/SelectShortcutIconActivity.kt:47`
- **Description:** `BitmapFactory.decodeStream(...)` result is used as non-null (`bitmap.byteCount`) with no null check. Failed/partial image picks NPE.
- **Suggestion:** Null-check `bitmap` and show `R.string.failed` / return early before touching `byteCount`.
- **Status:** resolved — failed bitmap decoding is checked and reported without dereferencing null.

### Issue 7 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/ShortcutLauncherFolderActivity.kt:207`
- **Description:** `requestWindowFeature(FEATURE_NO_TITLE)` runs in `showFolder()` after `super.onCreate()` (and after theme processing). Window features must be requested before the window is created/content is set; this call is a no-op or throws depending on path, so folder title-bar hiding is broken vs intended no-title UX.
- **Suggestion:** Request no-title via theme (`windowNoTitle` / dialog theme) or call `supportRequestWindowFeature` before `super.onCreate`, not after.
- **Status:** resolved — the no-title feature is requested before `super.onCreate()` for folder mode.

### Issue 8 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/Main.kt:248`
- **Description:** FAB previously inflated a `PopupMenu` at the button with `onPrepareMainOptionsMenu` / `onMainOptionsItemSelected`. It now calls `openOptionsMenu()`, which depends on the ActionBar overflow and often does nothing useful on devices without a hardware menu key or when the panel is not shown. Functional regression for the main entry point to filters/settings actions.
- **Suggestion:** Restore a Compose `DropdownMenu` / AppCompat `PopupMenu` anchored to the FAB (or an overflow `IconButton` in a top app bar) using the existing menu XML or the same item handlers.
- **Status:** resolved — the AppCompat `PopupMenu` is again anchored to the main floating button.

### Issue 9 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/fragment/MainActivityAppListFragment.kt:40`
- **Description:** List/Grid are created inside Compose `AndroidView` `factory` only. Listeners/adapter set before the factory runs are applied when views are created, but `AndroidView` has no `update` block to re-bind adapter/listeners if the underlying view is recreated, and `setAppListAdapter` still races on null list/grid views.
- **Suggestion:** Add an `update` lambda that always re-applies adapter + listeners; or abandon AbsListView embedding and use pure Compose list state owned by `Main`.
- **Status:** resolved — ListView/GridView creation and binding are lifecycle-owned by the fragment rather than an `AndroidView` factory.

### Issue 10 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/ScheduledTasksAddActivity.kt:60`
- **Description:** Preference fragment is committed inside `AndroidView` `factory` via `commitNow()` when the container is first created. Same class of fragment-in-Compose issues as Main.
- **Suggestion:** Host `STAAFragment`/`STAATriggerFragment` in a non-Compose `FragmentContainerView` layout, or use a single lifecycle-aware attach path.
- **Status:** resolved — fragment attachment occurs after container attachment, reuses restored fragments, and avoids `commitNow()`.

### Issue 11 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/FirstTimeSetupActivity.kt:38`
- **Description:** Same pattern: Preference fragment transaction inside Compose `AndroidView` factory. First-run setup is especially sensitive to broken fragment attach (blank screen / crash).
- **Suggestion:** Keep a small XML shell with `FragmentContainerView` + Compose chrome, or use Fragment APIs outside composition side effects.
- **Status:** resolved — fragment attachment follows the same lifecycle-aware, idempotent path as Issue 10.

### Issue 12 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/viewmodel/AutoDiagnosisViewModel.kt:213`
- **Description:** Diagnosis items for device-owner and root both use id `"-3"` (and more duplicates elsewhere). The commit message claims diagnosis key fixes; `AutoDiagnosisActivity` avoids the crash only by not passing a `key` to `LazyColumn.items`. Using `id` as key later will still crash.
- **Suggestion:** Give every diagnosis row a unique key and use `items(problems, key = ...)`.
- **Status:** resolved — device-owner and root rows now have distinct IDs.

### Issue 13 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/compose/AppListItems.kt:26`
- **Description:** `DrawableImage` always converts `Drawable` → `Bitmap` via `getBitmapFromDrawable` on the composition thread. For adaptive icons / complex drawables this allocates full-size bitmaps per visible row and can mutate drawable bounds. Used heavily from main list, folder grid, shortcut picker, etc.
- **Suggestion:** Prefer `rememberDrawablePainter` / Coil; if bitmap conversion is required, do it once when building the model (off main thread).
- **Status:** resolved — drawables are painted directly without composition-thread bitmap rasterization.

### Issue 14 — Severity: bug

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/CommandExecutorActivity.kt:34`
- **Description:** Output field is hard-coded `value = ""` / `onValueChange = {}` and the finish button only calls `finish()`. Input is never executed. Parent was already a near-stub, but the Compose port still ships a non-functional executor UI that looks complete.
- **Suggestion:** Wire real command execution + output state, or do not present interactive fields that imply execution works.
- **Status:** acknowledged, no change — confirmed as a pre-existing and currently unreachable stub, not a migration regression.

### Issue 15 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/ScheduledTasksManageActivity.kt:221`
- **Description:** Enabling/disabling a time task updates DB then `TasksUtils.checkTimeTasks`. Trigger tasks only flip the DB flag and never call `TasksUtils.checkTriggerTasks`.
- **Suggestion:** When `!task.isTimeTask`, call `TasksUtils.checkTriggerTasks(this)` after update, matching save path in `ScheduledTasksAddActivity`.
- **Status:** resolved — trigger-task changes now call `TasksUtils.checkTriggerTasks()`.

### Issue 16 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/compose/FreezeYouTheme.kt:29`
- **Description:** Theme only overrides `primary`/`secondary` from `android.R.attr.colorAccent`. Material3 surfaces/background/onSurface stay at default M3 colors, so night mode and existing AppCompat colorPrimary/windowBackground themes can look inconsistent.
- **Suggestion:** Map `colorPrimary`, `colorPrimaryDark`, `windowBackground`, `textColorPrimary`, etc. into color schemes, or wrap content in `Surface` using theme-resolved colors.
- **Status:** resolved — AppCompat attributes are safely resolved into the Material color scheme and content background.

### Issue 17 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/ShortcutLauncherFolderActivity.kt:153`
- **Description:** Long-press popup uses `LocalView.current` as the anchor for `Support.showChooseActionPopupMenu`. That anchors to the Compose root/host view, not the specific grid cell, so the popup position can be wrong.
- **Suggestion:** Capture the item’s local coordinates / view, or use Compose `DropdownMenu`.
- **Status:** resolved — each folder item owns a local popup anchor.

### Issue 18 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/FUFLauncherShortcutCreator.kt:68`
- **Description:** Search lowercases the query with device locale but matches with `Locale.ROOT`, and no longer uses `processListFilter` (behavior not identical for locale-sensitive casing).
- **Suggestion:** Use one locale consistently, or share `processListFilter`.
- **Status:** resolved — query and candidate matching use the same API-compatible locale.

### Issue 19 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/ShowLogcatActivity.kt:33`
- **Description:** Log is shown in an editable `OutlinedTextField` without `readOnly = true`. Users can accidentally edit a huge log buffer; parent EditText was effectively a display field. Also drops auto-scroll-to-end.
- **Suggestion:** Mark read-only (or `SelectionContainer` + `Text`), and scroll to end when content updates.
- **Status:** resolved — log output is selectable, read-only text and scrolls to the end on update.

### Issue 20 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/adapter/MainAppListSimpleAdapter.kt:50`
- **Description:** `ComposeView` items never set `AbsListView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)`. Some API levels measure `ComposeView` children as 0-height or incorrect width inside `ListView`/`GridView`.
- **Suggestion:** Set list layout params explicitly; verify list and grid modes on API 23 and a current API.
- **Status:** resolved by Issue 4's native adapter replacement, with explicit `AbsListView.LayoutParams`.

### Issue 21 — Severity: suggestion

- **File:** `app/build.gradle:50`
- **Description:** `viewBinding = true` was removed and replaced with `compose = true`. No remaining view-binding references found (OK). No explicit `androidx.compose.ui:ui` dependency (only material3/tooling)—usually transitive, but brittle.
- **Suggestion:** Keep `implementation 'androidx.compose.ui:ui'` (and `foundation` if needed) explicitly on the Compose BOM.
- **Status:** resolved — Compose UI and Foundation are explicit BOM-managed dependencies.

### Issue 22 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/AutoDiagnosisActivity.kt:65`
- **Description:** `items(problems)` has no stable key. Combined with duplicate ids in the view model, refreshes can reuse wrong row state; the commit only “fixed” the crash by omitting keys rather than fixing data uniqueness.
- **Suggestion:** Unique keys per row (see Issue 12) and `items(problems, key = ...)`.
- **Status:** resolved — diagnosis rows use the corrected stable IDs as LazyColumn keys.

### Issue 23 — Severity: nit

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/compose/DialogContent.kt:30`
- **Description:** Checkbox label `Text(label)` is not clickable; only the box toggles. Old `CheckBox` widget toggled when tapping text.
- **Suggestion:** Make the whole row toggle the checkbox.
- **Status:** resolved — the whole labeled row toggles once; the checkbox itself is display-controlled.

### Issue 24 — Severity: nit

- **File:** `app/src/main/java/cf/playhi/freezeyou/ui/BackupMainActivity.kt:46`
- **Description:** Copy/paste buttons use `getString(android.R.string.copy)` inside a composable without `stringResource`, so they will not recompose on locale changes the same way as Compose resource reads.
- **Suggestion:** Use `stringResource(android.R.string.copy)` / `paste`.
- **Status:** resolved — Compose resource reads use `stringResource()`.

### Issue 25 — Severity: suggestion

- **File:** `app/src/main/java/cf/playhi/freezeyou/Main.kt:108`
- **Description:** Activity fields use `mutableStateOf` / `mutableIntStateOf` as delegates outside a Compose-managed store. Works with `setContent` but is easy to break (no `rememberSaveable`, concurrent writes from background via `runOnUiThread`). Search/filter state is not saved across rotation.
- **Suggestion:** Prefer a `ViewModel` + `collectAsState` / `rememberSaveable` for `searchQuery` and filter mode.
- **Status:** resolved — search text is saved/restored and the restored list fragment is reused by tag.

---

## Unexpected UX / UI changes in the original commit

This section records the original review observations. The remediation restores the confirmed parity regressions, including the main popup/FAB behavior, long-press motion, app-row dimensions and marquee, fast scrolling, scheduled-task selection chrome, AppLock borderless action, picker read-only behavior, and unexpected divider removal. Residual Material/Compose differences remain appropriate targets for product review rather than correctness blockers.

These are user-visible differences vs the pre-migration XML/AppCompat UI. Many are not “crashes” but will feel like a different app unless intentional.

### Global / cross-cutting

| Area | Before (XML / AppCompat) | After (Compose / Material3) | Impact |
|------|--------------------------|-----------------------------|--------|
| **Visual language** | AppCompat widgets, app themes, selectors, custom drawables (`shapedotblack`, `oval_ripple`, `applist_selector`) | Material3 `Button`, `OutlinedTextField`, `FloatingActionButton`, `Switch`, `HorizontalDivider` defaults | Entire app looks “M3 stock”, not FreezeYou-themed |
| **Theme colors** | `processSetTheme` + AppCompat attrs (`colorPrimary`, accent, window background) drive UI | `FreezeYouTheme` only maps **accent → primary/secondary**; surfaces/onSurface stay M3 defaults | Night mode / custom themes: wrong scaffold bg, low-contrast text, accent-only tint |
| **Typography** | Per-layout `textSize` / `textStyle` / Material3 text appearances in XML | Default M3 `Text` unless hardcoded `sp` | Title hierarchy and package-name size often wrong |
| **Dividers / list chrome** | Many lists used `divider="@null"` / `dividerHeight="0"` (flat list) | Widespread `HorizontalDivider` after each row | New hard lines between items on many screens |
| **Fast scroll** | Main list / FUF picker / STMA list: `fastScrollEnabled="true"` | Compose `LazyColumn` / embedded AbsListView without matching fast-scroll UX on pure Compose lists | Harder to jump through long lists |
| **Marquee / long labels** | Main row name/package: marquee forever; grid labels marquee; package lists bold+12sp marquee | `TextOverflow.Ellipsis` + maxLines=1 | Long app names no longer scroll; truncated with `…` |
| **Ripple / pressed / selected** | List selectors, FAB elevators (`fab_animator`), ImageButton backgrounds | Compose default ripple; selection often custom gray alpha or **missing** | Selection feedback and FAB feel changed |
| **Accessibility / contentDescription** | Icons often had descriptions (`@string/add`, status, etc.) | Several icons use `null` contentDescription (list status dots, shortcut icon grid) | TalkBack regressions |

### Main screen (`Main` + app list)

| Change | Detail |
|--------|--------|
| **Search field** | `EditText` with light padding / translucent tint (v21) → Material3 `OutlinedTextField` with floating label, borders, denser vertical footprint |
| **Search autofill** | `android:autofillHints="pkgLabel"` dropped |
| **Loading UX** | Loading lived in a full-screen `FrameLayout` while list container was **hidden**; caution text was bold, progress was a classic centered `ProgressBar` (80dp) with dimmed status text | Compose overlay: light translucent scrim + M3 `CircularProgressIndicator` + non-bold caution; list can still be “present” under overlay architecture |
| **FAB style** | Custom circular `ImageButton` (`shapedotblack`, 55dp, elevation 6, `fab_animator` stateListAnimator, `ic_action_add`) | Stock M3 `FloatingActionButton` + `Icon` |
| **FAB interaction** | **Tap:** `PopupMenu` anchored to the button with rotate 0°→45° animation; **long-press:** toggle alpha 1.0 ↔ 0.2 for less intrusive UI | **Tap:** `openOptionsMenu()` (ActionBar/overflow dependent); **long-press:** gone |
| **List row layout** | Icon **40dp**, name **18sp bold**, package **12sp**, freeze status **10dp** dot aligned end | Icon **48dp**, default M3 text sizes (not 18/12), status icon **24dp** (much larger status marker) |
| **List margins** | Row `layout_margin` 5dp, icon margin 5dp | Padding 8dp row; different spacing rhythm |
| **Selection highlight** | `R.color.translucentGreyBackground` via adapter / selector | `Color.Gray.copy(alpha = 0.25f)` — similar idea, not the same token |
| **Grid labels** | Custom `IsSelectedAlwaysTrueTextView` + marquee 12sp under system `app_icon_size` | 52dp icon + default Text, ellipsis, no marquee |
| **Grid spacing** | XML-driven column width / spacing | Programmatic `GridView` in Compose: density-based vertical spacing 6dp, etc. — may not match old density feel |

### Scheduled tasks (`ScheduledTasksManageActivity`)

| Change | Detail |
|--------|--------|
| **Multi-select model** | System **ActionMode** (CAB) with menu select-all / invert / delete, ListView `multipleChoiceModal`, activated-state background (`applist_selector`) | Custom long-press selection + top **Button** bar (count / select all / invert / delete); **no activated row background** — hard to see what is selected |
| **FAB cluster** | Three ImageButtons: main 55dp black circle + two 35dp almost-white sub-FABs, **alpha fade** 150ms, margins 25/35 | M3 FAB + `SmallFloatingActionButton` + `AnimatedVisibility` (different sizes, colors, motion) |
| **Row style** | MaterialTextView TitleLarge / TitleSmall + `SwitchMaterial` (`focusable=false` so row clicks work) | Compose `Text` 20sp medium + body + M3 `Switch` — switch may compete with row click/long-click differently |
| **List chrome** | No divider | `HorizontalDivider` between tasks |

### App lock (`AppLockActivity`)

| Change | Detail |
|--------|--------|
| **Controls** | Only a large logo ImageView + **borderless colored** Unlock button at bottom | Logo **or** lock **IconButton** (96dp) **and** filled M3 Unlock button stacked |
| **Extra unlock affordance** | N/A (logo was clickable for auth in some paths) | Material **Lock** icon mid-screen is new UI chrome |
| **Button style** | `Widget.AppCompat.Button.Borderless.Colored` | Filled Material3 `Button` |

### Shortcut editor (`LauncherShortcutConfirmAndGenerateActivity`)

| Change | Detail |
|--------|--------|
| **Layout** | RelativeLayout + labeled sections with separate `TextView` labels above fields; package/target fields **not focusable** (pick-only) | `OutlinedTextField` for package/target (editable text) + “…” `Button`; different spacing (16dp padding, less 20dp section gaps) |
| **Icon preview** | Fixed layout size from XML | 140dp clickable `DrawableImage` |
| **Bottom actions** | Weighted classic buttons with a divider above the action row | `EqualButtons` row of three M3 filled buttons (Cancel / Generate / Simulate) — denser, different visual weight |
| **Package field** | Non-editable picker field | Editable `OutlinedTextField` — user can type free text (behavior change, not just look) |

### Folder shortcut (`ShortcutLauncherFolderActivity`)

| Change | Detail |
|--------|--------|
| **Create-shortcut UI** | Custom confirm icon/name dialog layout | Compose `AlertDialog` with icon + field |
| **Folder title bar** | Intended `FEATURE_NO_TITLE` | Request after `onCreate` → title bar likely still shows |
| **Long-press menu anchor** | Item view | Compose root view → menu position wrong |
| **Grid** | Custom item XML | `LazyVerticalGrid` Adaptive 72dp |

### About (`AboutActivity`)

| Change | Detail |
|--------|--------|
| **List** | `ListView` with **no dividers** | `LazyColumn` + **divider after every row** |
| **Slogan / version under title** | Slogan TextView (italic gray); list also contained version entry | Header shows **version code only** (`V {code}`); full version name still only in list row / toast path — visual hierarchy changed |
| **Row padding / type** | List item layouts (bold 18sp titles on other screens) | Generic 14dp vertical padding, default type |

### Auto diagnosis

| Change | Detail |
|--------|--------|
| **Row layout** | ConstraintLayout: title **18sp bold**, subtitle, status icon **end-aligned** with padding | Column: title + subtitle as default Text; status drawable **bottom-end** via `align(End)` under text — not side-by-side |
| **Progress** | Classic `ProgressBar` (indeterminate / determinate) | M3 `LinearProgressIndicator` always at top |
| **Progress hide** | Progress **GONE** at 100% | At 100%, determinate indicator branch ends; no explicit “hide forever” matching old GONE behavior as closely for intermediate states |

### Backup / import

| Change | Detail |
|--------|--------|
| **Backup main buttons** | AppCompat `Button` weight 1 in two rows | M3 filled buttons via `EqualButtons` / `ActionButton` |
| **Import chooser** | Custom switch list adapter + item layout | Compose `Switch` rows; similar structure, M3 switches |

### First-time setup / STAA prefs

| Change | Detail |
|--------|--------|
| **Finish button** | `Widget.AppCompat.Button.Colored` full width | Default M3 `Button` full width — color comes from incomplete theme map |
| **Prefs host** | XML `FrameLayout` shell | Compose column + fragment in `AndroidView` (chrome similar; theme of prefs vs Compose shell may clash) |

### Package / allow lists (`PackageListScreen`, notifications, URI auto-allow)

| Change | Detail |
|--------|--------|
| **Typography** | Name **18sp bold**, package **12sp**, marquee | Default M3 Text for both lines (often same weight/size feel) |
| **Empty state** | Placeholder “not available” rows | Same data pattern, different chrome |
| **Padding** | 10dp item padding | 16×10 dp in shared `PackageListScreen` |

### Manual mode

| Change | Detail |
|--------|--------|
| **Mode selector** | Button showing current mode | Still a button opening single-choice dialog (OK), but M3 button styling |
| **Freeze / Unfreeze** | Weighted AppCompat buttons, disabled until package set | M3 equal-weight buttons with `enabled` flag — similar logic, different look |

### Logcat / command executor / full-screen image

| Change | Detail |
|--------|--------|
| **Logcat** | Multi-line EditText, 12sp, margin 10dp, gravity start/top | M3 `OutlinedTextField` fillMaxSize — bordered box, **editable**, no auto-scroll to end |
| **Command executor** | Read-only output EditText + input + Finish | Looks similar but finish doesn’t run anything; output always empty |
| **Full-screen image** | Prior layout-specific image viewer | Compose `Image` Fit + tap to finish; action bar still configured — may leave chrome vs true immersive |

### Dialogs (always-allow)

| Change | Detail |
|--------|--------|
| **Checkbox control** | Platform/AppCompat `CheckBox` in inflated XML (`ipa_dialog_checkbox` / URI equivalent); label was part of checkbox | Compose `Checkbox` + separate `Text` (label not toggling); Material3 checkbox visuals |
| **Custom view height** | Code still zeros parent min height — OK | ComposeView measurement in dialog can add unexpected vertical space |

### Iconography & motion lost

| Lost / changed | Where |
|----------------|--------|
| FAB rotate animation on open menu | Main |
| FAB long-press fade (alpha) | Main |
| Sub-FAB alpha show/hide 150ms | Scheduled tasks |
| `stateListAnimator` / elevation animators on FABs | Main, STMA |
| Custom circular black FAB backgrounds | Main, STMA |
| List marquee for long package/app names | Main list/grid, package lists |
| Fast scroller | Main / FUF / STMA (where replaced by pure Compose) |
| ActionMode CAB multi-select | Scheduled tasks |
| Borderless unlock button | App lock |

---

## What improved or stayed intentional

- Main search uses a stable **`masterAppList`** clone (avoids wiping the source list; also avoids re-adding `TextWatcher`s every `generateList` completion).
- Compose plugin + BOM wiring is coherent; removing unused viewBinding looks OK.
- Shared helpers (`AppListItem`, `PackageListScreen`, `EqualButtons`) reduce duplication; theme attributes are now mapped safely into the Material color scheme.
- Diagnosis rows now have unique IDs and stable LazyColumn keys.

---

## Resolution order (completed)

1. [x] **Crash / security of core flows:** dialog always-allow lifecycle; API-23 locales; SelectShortcutIcon null bitmap.
2. [x] **Main UX parity:** restore the FAB-anchored `PopupMenu` and long-press/rotation behavior.
3. [x] **Main list architecture:** replace ComposeView-in-ListView rows and per-bind bitmap rasterization with a classic recycled view adapter.
4. [x] **Fragment hosting:** attach Main / FirstTimeSetup / STAA fragments only after container attachment, reuse restored fragments, and avoid transactions with state loss.
5. [x] **Theme parity:** map AppCompat theme colors into `FreezeYouTheme`.
6. [x] **Per-screen UX parity:** restore typography, status-dot sizing, divider behavior, selection chrome, read-only log display, picker behavior, and borderless AppLock action.
7. [x] **Diagnosis unique IDs + keys.**

---

## Recommended verification matrix (manual)

| Screen | Checks |
|--------|--------|
| Main | Search, filter menu via FAB, multi-select, freeze popup, grid vs list, night theme, rotation |
| URI freeze / Install packages | Always-allow checkbox visible, toggles, persists allow list |
| FUF shortcut creator | API 23 device if available; search; add to folder |
| Scheduled tasks | Enable switch, multi-select delete, add time/trigger FABs |
| Folder | Create shortcut, open folder, long-press action menu position, title bar |
| App lock | Biometric prompt, unlock controls |
| Auto diagnosis | Row layout/status icon, refresh, no crash |
| Backup import | Toggle items, import subset |
| Select shortcut icon | Large install base; pick gallery image edge cases |

---

## Artifacts

| Path | Notes |
|------|--------|
| This report | `.ai/review-reports/2026-08-09-c991de57-compose-migration-review.md` |
| Scratch review (session) | `/tmp/grok-1000/grok-review-b53c189c.md` |

---

## Appendix — Diff scale

```
80 files changed, 2259 insertions(+), 4023 deletions(-)
```

Deleted: nearly all `res/layout*` activity/list/item XML used by the migrated screens.  
Added: `ui/compose/*`, `res/values/ids.xml` (fragment container ids), Compose dependencies.
