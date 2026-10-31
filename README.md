# Abeer Keyboard (WePlay)

A simple Android Input Method (IME) prototype with letter styles, phrase saving, and a random style button.

## Build the APK using only your phone
1. Extract this ZIP.
2. In your phone browser, create a GitHub repository named `AbeerKeyboard`.
3. Upload the extracted project files/folders to the repository (keep folder structure).
4. Open the repository's **Actions** tab and run **Build Abeer Keyboard APK** (or wait for the push build).
5. Open the completed workflow run, download the `Abeer-Keyboard-APK` artifact, and extract it.
6. Install `app-debug.apk`. Android may ask permission to install unknown apps for the browser/file manager.
7. Enable it under Settings → System / Additional settings → Keyboard & input method → On-screen keyboard / Manage keyboards.
8. Open WePlay, tap the chat box, and select Abeer Keyboard from the keyboard switcher.

## Notes
- This is a prototype project, not a prebuilt APK. The APK is produced by GitHub Actions.
- The keyboard only inserts text when you tap **SEND**. It does not read or transmit what you type.
- Custom template uses `{x}` as a placeholder. Example `{x}{x}` doubles each letter. In this first prototype, the custom template is saved but the SEND action currently uses the selected built-in style; custom-template application is a planned next improvement.
- Fancy Unicode characters may display differently depending on WePlay's font support.
