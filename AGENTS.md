# UI text and feedback conventions

- Put user-visible text in `Cards/app/src/main/res/values/strings.xml` and provide its Slovak translation in `Cards/app/src/main/res/values-sk/strings.xml`. This includes accessibility labels and quick messages. Internal identifiers, log messages, and barcode format constants are not translated UI text.
- Keep every top-level resource in both strings files alphabetized by its `name`, including plurals. Insert new entries in their alphabetical position. Preserve equivalent format placeholders in both languages.
- Use `AppMessages` for transient feedback. Keep failures and outcomes the user cannot observe directly; do not add success messages for visibly completed edits, moves, selections, or deletions. Prefer inline field errors for invalid input. Do not display raw exception text.
- Run `:app:verifyLocalizedStrings` (also part of `preBuild`) after resource changes.

## Visual design

- Prefer restrained, theme-aware glass effects where overlapping or scrolling content benefits from visible depth. Keep controls legible and avoid applying translucency everywhere purely as decoration.

## Build and device deployment

- After app changes in this project, always build the debug APK and install it on the user's connected phone with ADB. This is the user's standing preference and authorization; do not ask again for each installation.
- Prefer the connected A059 phone. Resolve its current ADB serial each time because wireless IDs can change. If it is unavailable or multiple target phones are ambiguous, report that installation is pending instead of silently choosing another device.
- Preserve installed app data with an update installation (`adb install -r`). Never uninstall or clear app data to work around an installation failure without explicit user approval.
