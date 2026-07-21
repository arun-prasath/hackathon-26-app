# Implementation Plan - SC Mobile with Smart AI Router

Create an Android application that replicates the SC Mobile India UI and implements a "Smart Local AI Router" to optimize token usage and latency.

## User Review Required

> [!IMPORTANT]
> The UI will be a high-fidelity mock based on the provided screenshots. Some complex graphics (like the specific banner image or custom icons) will be approximated using Compose primitives and standard Material icons.

> [!NOTE]
> The "Smart Local AI Router" will be implemented as a simulated service to demonstrate the routing logic (Local vs. Remote) without requiring specific on-device LLM hardware/models during the initial build.

## Proposed Changes

### UI & Navigation
- **Theme**: Implement a custom `ScMobileTheme` with the primary Blue and Green colors.
- **Navigation**: Use Jetpack Compose Navigation for the flow: Login -> Biometric -> Home -> Logout.

### Components

#### [NEW] [ScMobileTheme.kt](file:///D:/Codebase/Personal/Hackathons/acc-3.0/projects/MobileBankingAssistant/app/src/main/java/com/example/mobilebankingassistant/ui/theme/ScMobileTheme.kt)
Define custom colors, typography, and shapes matching the SC Mobile branding.

#### [NEW] [LoginScreen.kt](file:///D:/Codebase/Personal/Hackathons/acc-3.0/projects/MobileBankingAssistant/app/src/main/java/com/example/mobilebankingassistant/ui/LoginScreen.kt)
Replicate the login screen with gradient backgrounds, "Welcome back" message, and biometric login triggers.

#### [NEW] [HomeScreen.kt](file:///D:/Codebase/Personal/Hackathons/acc-3.0/projects/MobileBankingAssistant/app/src/main/java/com/example/mobilebankingassistant/ui/HomeScreen.kt)
Implement the dashboard with:
- Top bar (Morning greeting, Notification, Chat, Logout).
- Promotion banner.
- Quick link icons (SC Invest, Pay Bills, etc.).
- Asset/Liability sections with expandable cards.
- Bottom Navigation bar.

#### [NEW] [SmartRouter.kt](file:///D:/Codebase/Personal/Hackathons/acc-3.0/projects/MobileBankingAssistant/app/src/main/java/com/example/mobilebankingassistant/logic/SmartRouter.kt)
Implement the `SmartIntentRouter` logic:
- `route(query: String)`: Determines if the task is "Trivial" (handled locally) or "Complex" (routed to backend).
- Simulate token savings calculation.

#### [NEW] [AssistantDialog.kt](file:///D:/Codebase/Personal/Hackathons/acc-3.0/projects/MobileBankingAssistant/app/src/main/java/com/example/mobilebankingassistant/ui/AssistantDialog.kt)
A chat interface to interact with the Smart Router and see it in action.

## Verification Plan

### Automated Tests
- Unit tests for `SmartIntentRouter` to verify routing logic for different intent types.
- Compose UI tests for navigation flow.

### Manual Verification
- Deploy to emulator/device.
- Verify the transition from Login to Home.
- Test the expandable sections in the Home screen.
- Use the Assistant to trigger different routing paths (Local vs. Remote) and observe the simulated token savings.
