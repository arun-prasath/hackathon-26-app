# LiteRT-LM Setup For This App

The app looks for this model file:

```text
gemma3-1b-it.litertlm
```

Supported locations:

```text
Internal app storage:
/data/data/com.demo.mobilebankingassistant/files/models/gemma3-1b-it.litertlm

App-specific external storage:
/sdcard/Android/data/com.demo.mobilebankingassistant/files/models/gemma3-1b-it.litertlm
```

## Import From Downloads

If the model is already in your phone Downloads folder:

1. Open the app.
2. Open **SC Smart Assistant**.
3. Tap **Import Local Model**.
4. Select the `.litertlm` file from Downloads.
5. Wait until the app shows:

```text
Model imported into private app storage.
```

The app copies the selected file into:

```text
/data/data/com.demo.mobilebankingassistant/files/models/gemma3-1b-it.litertlm
```

## ADB Push Alternative

For a physical phone or emulator, install and open the app once, then push the model with ADB:

```powershell
adb shell mkdir -p /sdcard/Android/data/com.demo.mobilebankingassistant/files/models
adb push C:\path\to\gemma3-1b-it.litertlm /sdcard/Android/data/com.demo.mobilebankingassistant/files/models/gemma3-1b-it.litertlm
```

Then reopen the assistant. It should show:

```text
Local model found
```

On the first query, LiteRT-LM initializes on a background thread. This may take several seconds.

The banking policy is still enforced in Kotlin:

- FAQ, glossary, greetings, app navigation, and low-risk cached lookup can route local.
- Transfers, PIN/password/security changes, profile changes, fraud, complaints, and credit decisions route backend.
- If the model is missing or initialization fails, the app falls back to rules-only routing.
