# SC Smart Banking Assistant

Android/Kotlin demo for a mobile banking assistant that routes low-risk requests to an on-device LiteRT-LM model and routes complex or sensitive requests to a backend AI/mock service.

The app is branded as **SC Smart Banking powered by AINigmas** and demonstrates a production-like flow for:

- local model import and initialization
- encrypted-cache style banking context
- local read-only account and statement answers
- backend service workflows such as address change and card status updates
- backend AI routing for complex questions
- mock SC Mobile screens for login, home, account, statements, services, PIN change, and logout

## Project Structure

```text
.
├── app/                         Android app module
├── mock-service/                Node.js mock banking service and backend AI proxy
├── LITERT_LM_SETUP.md           LiteRT-LM model setup notes
├── build.gradle.kts             Root Gradle build file
├── settings.gradle.kts          Gradle project settings
└── README.md
```

## Tech Stack

- Kotlin
- Jetpack Compose
- Android SDK 33 target/min for Android 13 demo devices
- LiteRT-LM Android dependency
- Node.js mock service
- Optional OpenAI-backed backend AI proxy through the mock service

Android package:

```text
com.demo.mobilebankingassistant
```

## Prerequisites

- Android Studio
- JDK 11 or compatible Android Studio bundled JDK
- Android 13 device or emulator
- Node.js for the mock service
- Optional: `.litertlm` model file named `gemma3-1b-it.litertlm`
- Optional: OpenAI API key for backend AI proxy

## Run The Mock Service

From the project root:

```powershell
cd mock-service
npm start
```

The service runs on:

```text
http://0.0.0.0:8080
```

For a physical phone, set the Android app base URL to your laptop Wi-Fi IP in:

```text
app/src/main/java/com/demo/mobilebankingassistant/data/MockServiceConfig.kt
```

Example:

```kotlin
const val BASE_URL = "http://192.168.1.7:8080"
```

For emulator-only testing, use:

```kotlin
const val BASE_URL = "http://10.0.2.2:8080"
```

Restart the Android app after changing the URL.

## Optional Backend AI Proxy

To enable backend AI responses through the mock service:

```powershell
cd mock-service
$env:OPENAI_API_KEY="sk-..."
$env:OPENAI_MODEL="gpt-4o-mini"
npm start
```

Backend AI endpoint:

```text
POST /api/ai/chat
```

If `OPENAI_API_KEY` is not set, backend AI requests return a mock-service error message.

## Build The Android App

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

Or open the project in Android Studio and run the `app` configuration on a phone/emulator.

## LiteRT-LM Model Setup

The app expects this model filename:

```text
gemma3-1b-it.litertlm
```

Recommended demo flow:

1. Copy/download the `.litertlm` file to the phone Downloads folder.
2. Open the app.
3. Open **SC Smart Assistant**.
4. Tap **Import**.
5. Select the model from Downloads.
6. Wait for:

```text
Model imported into private app storage.
```

The app copies the model into private app storage:

```text
/data/user/0/com.demo.mobilebankingassistant/files/models/gemma3-1b-it.litertlm
```

More details are in:

```text
LITERT_LM_SETUP.md
```

## Main Demo Flows

- Login with password or biometric mock screen.
- View home dashboard with deposits and credit-card liabilities.
- Expand deposits and open statement details.
- Ask the assistant balance, spending, product, and service questions.
- Import and initialize local LiteRT-LM model.
- Change debit/ATM PIN through secure app flow.
- Submit address change request and receive a mock service request number.
- Block/unblock credit card from chat.
- Route complex questions to backend AI.

## Example Assistant Prompts

Local/cache-friendly:

```text
What is my balance?
List my last 5 transactions
Show my credit card spending pattern
Show a chart for my credit card spending
Find my fuel transactions
How much did I spend on groceries? Compare 3 months and give suggestions.
What products are available for fixed deposit?
Tell me about home loan interest rate.
```

Workflow/action:

```text
Change my ATM PIN
Change my address
Block my credit card
Unblock my credit card
```

Backend AI examples:

```text
Help me decide between personal loan and credit card EMI.
I want to complain about an unauthorized transaction.
Can you draft a financial plan for buying a car next year?
Why was my loan application rejected?
```

## Routing Behavior

The app uses a hybrid approach:

- **Local LLM + cache** for low-risk read-only questions.
- **App workflow** for controlled actions such as PIN change navigation, address request submission, and card block/unblock.
- **Backend AI** for sensitive, complex, or high-risk requests.
- **Rules-only fallback** if the local model is missing or returns invalid output.

For statement analytics, the app sends the user question plus account/credit-card statement context to the local LLM. If the model returns placeholder or unusable text, the UI guards against showing it and falls back to a cached statement summary.

## Mock Service Data

The mock service returns:

- logged-in user profile
- savings account details
- savings transactions
- debit card details
- credit card status and liabilities
- 3 months of credit-card statement data
- banking products such as mutual funds, fixed deposits, home loan, personal loan, and car loan

Mock service files:

```text
mock-service/server.js
mock-service/README.md
```

## Useful Commands

Build Android debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Run mock service:

```powershell
cd mock-service
npm start
```

Check mock service syntax:

```powershell
node --check mock-service/server.js
```

## Notes

This is a hackathon/demo project. It mimics a mobile banking local-AI architecture, but it is not production banking software.

Production hardening would require stronger model/package signing, secure model distribution, encrypted local storage, formal policy enforcement, real authentication, audit trails, server-side authorization, prompt-injection test suites, and regulatory review.
