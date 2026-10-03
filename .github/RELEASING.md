# Android releases

The [Android release workflow](workflows/release.yml) builds and attaches a signed
APK whenever a GitHub release or prerelease is published. It checks out the release
event's commit, builds `:app:assembleRelease`, signs and verifies the APK, and uploads:

- `app-release.apk`, preserving the existing release asset name.
- `app-release.apk.sha256`, a SHA-256 checksum for the signed APK.

Release titles, notes, and prerelease status remain under the maintainer's control.
Normal pushes and pull requests continue to use the separate Android CI workflow.

## Signing isolation

The workflow uses two jobs, each on a separate GitHub-hosted virtual machine:

- **Build unsigned APK** checks out the release source and runs Gradle with a
  read-only repository token. It has no release environment or signing secrets.
  It uploads only the unsigned APK as an Actions artifact, retained for seven days.
- **Sign and publish APK** uses the `release` environment. It downloads that exact
  artifact by ID from the same workflow run and fails if its digest does not match.
  It installs signing tools on its fresh runner, signs and verifies the APK, and
  uploads the release assets. It does not check out project code, run Gradle, or
  restore build caches.

Signing secrets are passed only to the signing step. The decoded keystore has
restricted file permissions, stays outside cached and uploaded paths, and is
removed when that step exits. Public signing certificate details appear in the
verification log and in the APK; the private key and passwords are not printed.

This separation prevents build processes from surviving into the signing job.
The signing tools, workflow, and release source still need to be trusted: isolation
does not stop a malicious app change from being included in the APK being signed.

## One-time setup

In **Settings → Environments**, create an environment named **`release`**. Configure
its deployment branch/tag rules for your trusted release refs, and add required
reviewers to control access to the signing key. Environment approvals must happen
before the signing job can start. Merely naming the environment in YAML does not
configure these protections; see [GitHub's environment documentation](https://docs.github.com/en/actions/reference/workflows-and-actions/deployments-and-environments).

Automatic release runs use the release tag as their ref. Manual runs use the
branch/tag selected in **Run workflow**, which is separate from the `tag` input.
The environment rules must allow that workflow ref. When reviewing a manual run,
also check the requested release tag and its source commit.

Add the following **environment secrets** inside `release`:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded contents of the existing release keystore. |
| `RELEASE_STORE_PASSWORD` | Keystore password. |
| `RELEASE_KEY_ALIAS` | Alias of the existing release signing key. |
| `RELEASE_KEY_PASSWORD` | Password for that key, even if identical to the keystore password. |

If these secrets already exist at repository level, add them to `release` and
remove the repository copies. Remove any organization-level grants of the same
signing credentials to this repository as well. Leaving copies outside the
environment lets other workflows request them without its protections.

Use the **same key as previous GitHub APKs (SIGNATURE P)** so users can update their
installed copies. A debug key, a new key, or the Google Play upload key is not a
replacement for that key. Never commit the keystore or passwords to the repository.

To encode the existing keystore on Windows, run this in a local PowerShell session
and paste the clipboard into the `RELEASE_KEYSTORE_BASE64` secret:

```powershell
$releaseKeystorePath = 'C:\path\to\existing-release.keystore'
[Convert]::ToBase64String([IO.File]::ReadAllBytes($releaseKeystorePath)) | Set-Clipboard
```

On Linux or macOS, an authenticated GitHub CLI can set the secret directly:

```sh
base64 < /path/to/existing-release.keystore | tr -d '\n' |
  gh secret set RELEASE_KEYSTORE_BASE64 --env release --repo FreezeYou/FreezeYou
```

The workflow uses the automatic `GITHUB_TOKEN`; no personal access token is needed.
The build job has `contents: read`, and only the signing/publishing job has
`contents: write`. Repository or organization policy must allow the workflow's
actions and these permissions. Protect changes to the workflow and release tags
with your repository's review and access rules.

## Publish a release

1. Merge the workflow into the default branch before creating the release tag.
2. Update `versionCode` and `versionName` in `app/build.gradle`, and commit them with
   the changes to release. The workflow uses these values without changing them.
3. Create a release on GitHub using a tag at that commit (the existing naming style
   is `V11.5(151)`), write the notes, and choose prerelease if appropriate.
4. Click **Publish release**. After the build, approve the `release` environment
   deployment if required. The APK and checksum appear after the **Android
   release** run succeeds; they are not available immediately when publishing.

The tag must include the workflow. The JDK version matches
`gradle/gradle-daemon-jvm.properties`; the SDK platform and build-tools version in
the workflow must be updated alongside the corresponding settings in `app/build.gradle`.

## Retry or build for a draft

After correcting a failed run's cause (for example, missing secrets), use **Re-run
failed jobs**, or open **Actions → Android release → Run workflow** and supply the
tag of an existing release. Manual runs check out `refs/tags/<tag>` and use the
toolchain configured in the selected workflow. For a draft release, push its tag
first: a tag entered only in an unpublished draft may not exist in Git yet.

Re-running only the signing job reuses the successful build's artifact ID. If the
artifact has expired after seven days, re-run all jobs or start a new manual run.
Re-running all jobs creates a new artifact for that attempt.

Successful retries replace only `app-release.apk` and `app-release.apk.sha256` via
`gh release upload --clobber`. Other assets and release notes are retained.

If immutable releases are enabled, GitHub does not allow assets to be added after
publication. Use the manual workflow against a draft, wait for the assets, then
publish it. The automatic run detects an immutable release and stops before building.

To verify a downloaded APK's checksum, put both assets in the same directory and run:

```sh
sha256sum --check app-release.apk.sha256
```
