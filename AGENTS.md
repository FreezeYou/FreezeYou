# Agent instructions

## Commits

Create a commit only when the user asks for one. Leave `user.name` and `user.email` unchanged. The repository user stays the git author. The assisting agent is recorded only with a `Co-Authored-By` trailer.

### Message

Use an imperative subject of about 72 characters, then a blank line, then a body that stands alone. The body must say:

- Why the change was needed, including the failure or requirement it addresses.
- What changed, naming the important files, components, or behavior.
- What was deliberately left unchanged when that matters, such as a public component name, on-disk format, or existing owner.
- How it was checked, and what was not run.

When the change fixes, follows, reverts, or was caused by an earlier commit, cite that commit. Use its short hash and subject. For a direct fix or follow-up, put the hash in the subject, as in `Fix DPM admin component introduced by 1d33e7a3`. Otherwise name it in the body. Look the hash up with `git log`. Do not invent a relationship to whichever commit happens to be `HEAD`.

Do not use a subject that only says the work was done, such as `Update code` or `Fix bug`. Do not put the trailer text in the subject.

### Co-Authored-By

End every agent-created commit with one trailer for each agent that materially wrote the change:

```text
Co-Authored-By: Agent Name <email>
```

Leave one blank line between the body and the trailer. Use the agent's own name and version, not the git author's name and not a generic label such as `AI` or `Cursor`.

Known identities in this repository:

| Agent | Trailer |
| --- | --- |
| Grok | `Co-Authored-By: Grok <version> <grok@x.ai>` |
| Codex / GPT | `Co-Authored-By: Codex (GPT-<version>) <noreply@openai.com>` or the exact `GPT-<version> <gpt-<version>@openai.com>` form already used on the branch |
| Gemini | `Co-Authored-By: Gemini <version or model name> <gemini-<model>@google.com>` |

Match an identity already present in `git log` when the same agent has committed here before. For Grok, keep the email `grok@x.ai` and put the model version in the name, for example `Co-Authored-By: Grok 4.7 <grok@x.ai>`.

### Example

```text
Fix DPM admin component introduced by 1d33e7a3

The Java-to-Kotlin migration in 1d33e7a3 (after 487e3ce0) imported
android.app.admin.DeviceAdminReceiver while keeping the app receiver's
simple name. Inside DeviceAdminReceiver.getComponentName(), that import
made DeviceAdminReceiver::class.java compile as the framework class.

DevicePolicyManager then received a component that is not the active
admin, so DPM hide and unhide threw SecurityException. Alias the
framework import so the class literal is the app receiver again. The
manifest component is unchanged, so an existing device owner stays valid.

Verified with kotlinc and javap. Did not install an APK.

Co-Authored-By: Grok 4.7 <grok@x.ai>
```
