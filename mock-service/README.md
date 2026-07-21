# SC Mobile Mock Service

Mock HTTP service for the Android banking demo.

## Run

```powershell
cd mock-service
npm start
```

To enable backend AI responses, set your OpenAI API key before starting:

```powershell
$env:OPENAI_API_KEY="sk-..."
$env:OPENAI_MODEL="gpt-4o-mini"
npm start
```

## Endpoints

- `GET /health`
- `GET /api/user/demo`
- `GET /api/model/manifest`
- `GET /api/model/package`
- `POST /api/ai/chat`

## Android URLs

For Android Emulator, the app uses:

```text
http://10.0.2.2:8080
```

For a physical phone, run the service on your laptop and change `MockBankingRepository` base URL to your laptop Wi-Fi IP, for example:

```text
http://192.168.1.25:8080
```

The Android app has fallback demo data, so the UI still works when the service is unreachable.
