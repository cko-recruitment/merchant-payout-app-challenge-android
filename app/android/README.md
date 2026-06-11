# Merchant Mobile Banking - Android Challenge

## The Challenge

You are building a slice of a merchant banking application for Android. The app allows a merchant to view their account balance, browse transaction history, and initiate payouts to bank accounts.

This is a **take-home challenge**. You have 3-4 hours to complete as many steps as you can. Quality and reasoning matter more than quantity. A well-crafted solution to Steps 1-3 is stronger than a rushed attempt at all six.

## Setup

- Create a new **Android Studio project** (Jetpack Compose - required, minimum SDK 26 / API 26+)
- Language: **Kotlin**, targeting API 34+
- No third-party networking libraries required - use `HttpURLConnection` or `OkHttp` (your choice)
- Dependency injection is optional - plain constructor injection is fine
- Load data from the JSON fixtures provided in `fixtures/` (see below)

## Fixtures

The `fixtures/` directory contains static JSON files that mirror the real API contract. Add them to your app's `assets/` folder and load them with `context.assets.open(filename)`.

| File | Endpoint equivalent | Description |
|---|---|---|
| `merchant.json` | `GET /api/merchant` | Balance and 3 most recent activities |
| `activity_page1.json` | `GET /api/merchant/activity` (page 1) | 15 items, `has_more: true` |
| `activity_page2.json` | `GET /api/merchant/activity?cursor=act_015` (page 2) | 15 items, `has_more: false` |
| `payout_success.json` | `POST /api/payouts` (success) | Successful payout response |

### Data Contract

All monetary amounts are in **pence** (the lowest denomination). £50.00 = `5000`.

```kotlin
enum class Currency { GBP, EUR }
enum class ActivityType { payout, deposit, refund, fee }
enum class ActivityStatus { completed, pending, processing, failed }
enum class PayoutStatus { pending, processing, completed, failed }

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val amount: Int,          // in pence, negative for outflows
    val currency: Currency,
    val date: String,         // ISO 8601
    val description: String,
    val status: ActivityStatus
)

data class MerchantData(
    val available_balance: Int,
    val pending_balance: Int,
    val currency: Currency,
    val activity: List<ActivityItem>
)

data class PaginatedActivityResponse(
    val items: List<ActivityItem>,
    val next_cursor: String?,
    val has_more: Boolean
)

data class PayoutResponse(
    val id: String,
    val status: PayoutStatus,
    val amount: Int,
    val currency: Currency,
    val iban: String,
    val created_at: String
)
```

JSON parsing: use `kotlinx.serialization` (recommended), `Moshi`, or `Gson` - your choice.

## Reference Designs

See `../docs/android/` for reference screenshots showing the expected UI for each step.

## Required Architecture

Follow Google's recommended layered architecture:

- **Data layer**: `MerchantRepository` and `DeviceIdentityRepository` as interfaces with concrete implementations. No ViewModel or Composable should access a data source directly.
- **Domain layer**: Business rules (the £1,000 biometric threshold, IBAN validation, and pence/pounds conversion) must live in dedicated, testable types. Not in ViewModels or Composables.
- **UI layer**: ViewModels expose a single `uiState: StateFlow<UiState>` using `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)`. Composables collect state with `collectAsStateWithLifecycle()`, not `collectAsState()`. Use `SharedFlow(replay = 0)` for one-shot navigation and UI events, not `StateFlow`, which replays the last value on resubscription.

## Steps

### Step 1 - Merchant Home Screen

Build the main screen showing:

- Available balance, formatted as currency (e.g. £5,000.00)
- Pending balance
- The 3 most recent activity items from `merchant.json`
- A "View All Transactions" button

Handle loading and error states. The UI should not show stale or broken content.

Load balance and activity **concurrently, not sequentially**. Use `async`/`await` with `coroutineScope`. A slow activity fetch must not delay the balance from rendering.

Collect your ViewModel's `StateFlow` in Composables using `collectAsStateWithLifecycle()`, not `collectAsState()`.

**What we're evaluating:** ViewModel + `StateFlow`/`UiState`, `stateIn` + `WhileSubscribed(5_000)`, `async`/`await` for concurrent loading, `collectAsStateWithLifecycle()`, `NumberFormat` or `BigDecimal` for currency formatting, separation of concerns.

### Step 2 - Transaction List (Paginated)

When the user taps "View All Transactions", navigate to a screen with a scrollable list. Implement cursor-based pagination:

- Load `activity_page1.json` on first display
- When the user scrolls near the end of the list, simulate fetching the next page using `activity_page2.json` (use `next_cursor` from page 1 to decide which file to load)
- Show a loading indicator at the bottom while the next page loads
- Group transactions by the merchant's **local date** (Today / Yesterday / 18 May). Use `LocalDate` with `ZoneId.systemDefault()`. Do not compare ISO string prefixes.

**What we're evaluating:** Pagination state management in ViewModel, `LazyColumn` with `key {}` for recycling, scroll position trigger (`LazyListState`), avoiding duplicate fetches, `LocalDate` timezone-correct date grouping, Compose stability (`@Stable`/`@Immutable` or `ImmutableList`).

### Step 3 - Payout Form & Confirmation

Build a two-step payout flow:

**Step 3a - Form:**
- Currency selector: GBP or EUR
- Amount input: display in pounds (£), store and submit in pence
- IBAN input: basic validation - not empty, starts with two uppercase letters, minimum 15 characters
- Inline validation errors on submit

**Step 3b - Confirmation:**
- Summary screen showing amount, currency, and masked IBAN before final submission
- On confirm, load `payout_success.json` and show a success state
- Handle two specific error cases (simulate by checking the amount before "submitting"):
  - Amount == 99999 pence: service unavailable (show a generic retry message)
  - Amount == 88888 pence: insufficient funds (show a specific "Insufficient funds" message)

**Additional requirements:**

1. The payout form state (amount, IBAN, currency) must survive **process death**. Use `SavedStateHandle` in your ViewModel, not `rememberSaveable`, which only survives configuration changes.

2. The Confirm button must be **disabled** and show a "No internet connection" message when the device is offline. Use `ConnectivityManager.NetworkCallback` to observe connectivity in real time.

3. Pressing Back on the confirmation screen must return the user to the form. After a confirmed payout, navigating Home must **clear the entire payout flow from the back stack**. Use `popUpTo` with `inclusive = true` so the merchant cannot press back into the confirmation screen.

**What we're evaluating:** `BigDecimal` for currency math (not `Double` or `Float`), two-step UX pattern, `sealed class` or `sealed interface` for UI state, error state differentiation, `SavedStateHandle` for process death persistence, `ConnectivityManager.NetworkCallback` for offline state, `popUpTo` back stack management.

### Step 4 - Device Identity

Before a payout can be submitted, the app must attach a `device_id` to the request.

Implement a `DeviceIdentityRepository` that:

- Generates a `UUID` on first launch
- Persists it in **`EncryptedSharedPreferences`** (backed by the Android Keystore), not plain `SharedPreferences`
- Returns the same ID on subsequent launches

Include the `device_id` as a field in your payout submission logic.

Set `android:allowBackup="false"` in your `AndroidManifest.xml`.

**What we're evaluating:** `EncryptedSharedPreferences` / `KeyStore` usage, understanding of why encrypted storage is appropriate for a device fingerprint, correct initialisation lifecycle, `allowBackup` threat model.

### Step 5 - Biometric Authentication

For payouts **over £1,000** (100,000 pence), require biometric authentication before the confirmation screen is shown.

- Use the `BiometricPrompt` API (`androidx.biometric`) with `BiometricManager.Authenticators.BIOMETRIC_STRONG` for the capability check
- Handle three outcomes: success, user cancellation, biometrics unavailable (show a fallback message)
- The £1,000 business rule must be **testable independently of hardware**. Abstract the biometric call behind a `BiometricAuthenticator` interface that is injected into the ViewModel
- **Include at least one unit test** that verifies the threshold: a £999 payout skips biometrics, a £1,001 payout triggers it
- Ensure UI updates after the authentication callback happen on the **main thread**

**What we're evaluating:** `BiometricPrompt` + `BiometricManager.canAuthenticate`, `BIOMETRIC_STRONG` vs `BIOMETRIC_WEAK` reasoning, interface abstraction for testability, unit test coverage of the business rule, `Dispatchers.Main` for UI updates, lifecycle safety.

### Step 6 - Screenshot Prevention

When the user is on the Payout Form or Confirmation screen, **prevent screenshots and screen recordings** entirely.

- Apply `WindowManager.LayoutParams.FLAG_SECURE` to the window when these screens are active. Add the flag in `onResume` and remove it in `onPause`, not in `Application.onCreate`
- If your release build uses R8/ProGuard, add a keep rule for any biometric or security classes accessed via reflection and verify your release build works before submitting

**What we're evaluating:** `FLAG_SECURE` per-screen scoping (not global), `onResume`/`onPause` lifecycle correctness, ProGuard/R8 awareness. Be ready to discuss why Android can block capture while iOS can only detect it.

## Submission

Push to a private GitHub repository and share access with your interviewer.

Include a `NOTES.md` at the root of your project with:
- How to build and run the project
- Key architectural decisions and why you made them
- What you would improve or add with more time
- Anything you deliberately left out and why

## What We Care About

| Area | What to demonstrate |
|---|---|
| Architecture | ViewModel + `StateFlow`, clean separation of data/logic/UI |
| Kotlin idioms | Coroutines, `Flow`, data classes, sealed classes, `?.let`, `Result<T>` |
| Currency safety | Never use `Double` or `Float` for monetary values |
| Security | Encrypted storage, correct biometric flow |
| Error handling | Distinct states for loading, empty, error - not just happy path |
| Testing | At least one unit test covering business logic (e.g. payout validation, currency formatting) |
| Communication | Clear `NOTES.md` with trade-offs |

We do **not** evaluate: pixel-perfect UI, animation polish, specific DI framework choices.
