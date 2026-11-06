---
name: unit-test
description: "Unit test conventions for the merchant payout android project. Use when writing/reviewing JUnit 5 + MockK unit tests: configuring `MainDispatcherExtension` for coroutines, mocking with strict `mockk()`, enforcing the Given/When/Then pattern with backticked test names, applying `exactly = N` to `verify`/`coVerify`, and testing `StateFlow`s."
---

## 1. Test class configuration

### 1.1 Coroutine extensions
- For tests that involve coroutines (almost all of them), use `MainDispatcherExtension` with `@ExtendWith(MainDispatcherExtension::class)`. It lives in the test fixtures of `:domain` (`manoellribeiro.dev.domain.testing.MainDispatcherExtension`) and is shared by `:domain`, `:data` and `:app`
- The extension replaces `Dispatchers.Main` with a `StandardTestDispatcher` before each test. Every `TestDispatcher` created after that (including the one inside `runTest`) automatically shares its scheduler, so `advanceUntilIdle()` controls all of them. Without the extension, each dispatcher gets its own scheduler and the test loses control of virtual time.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class HomeViewModelTest {}
```

### 1.1.1 Coroutine opt-in
- Test classes that use experimental coroutine test APIs (`UnconfinedTestDispatcher`, `advanceUntilIdle`, `backgroundScope`) are annotated with `@OptIn(ExperimentalCoroutinesApi::class)` on the class, so the build output stays free of warnings

### 1.2 Dependency injection
- You should never create a dependency injection context (`AppContainer`) inside the unit tests. If there is a class that needs it to be tested, skip that class and report it to the user so it can be refactored first.
- You should never share dependencies between tests, all the tests should run with new instantiated mocked dependencies inside the `setup` method with `@BeforeEach` annotation.

## 2. Setup and teardown

### 2.1 `setup()` method (@BeforeEach)
- Always use `@BeforeEach` to configure the test environment
- Initialize mocks and dependencies, the mock behaviors should be created inside each unit test method never in the `setup` method (the only exception is described below).
- Always create the class under test in `setup`, never inside a test method or a helper. The only exception is a plain data class under test whose constructor receives the fake values (e.g. `PayoutRequest`, `Payout`): declare the fake data at the start of the test and create the instance right after it, inside the test method.
- Whenever the class under test needs a `CoroutineDispatcher`, pass `UnconfinedTestDispatcher()` as a constructor parameter
- The only mock behavior allowed in `setup` is the one the class under test needs to be constructed: if it calls a dependency in its constructor or property initializers (e.g. `PayoutViewModel` calls `isPayoutFormComplete` to build its initial `uiState`), a strict mock throws unless that call is configured before the constructor runs. Configure only that call, right before creating the class, and make it return something each test can control (see 2.2). Everything else stays in the test methods.

```kotlin
private lateinit var preferences: SharedPreferences
private lateinit var repository: DeviceIdentityRepositoryImpl

@BeforeEach
fun setup() {
    // Initialize mocks
    preferences = mockk()

    // Create the class under test
    repository = DeviceIdentityRepositoryImpl(
        preferencesProvider = { preferences },
        ioDispatcher = UnconfinedTestDispatcher(),
    )
}
```

### 2.2 Dependencies called when the class is created
- When the constructor-time call returns a `Flow`, back it with a `Channel` created in `setup`, so the class is still created in `setup` and each test decides what the flow emits or throws
- No class in this project does this today, so the example shows how it would look if `PayoutViewModel` started collecting `observeIsOnline` when it is created

```kotlin
private lateinit var viewModel: PayoutViewModel
private lateinit var observeIsOnline: ObserveIsOnlineUseCase
private lateinit var fakeIsOnlineChannel: Channel<Boolean>

@BeforeEach
fun setup() {
    observeIsOnline = mockk()

    // Only behavior configured in setup: PayoutViewModel calls it when it is created
    fakeIsOnlineChannel = Channel(Channel.UNLIMITED)
    every { observeIsOnline() } returns fakeIsOnlineChannel.receiveAsFlow()

    viewModel = PayoutViewModel(
        observeIsOnline = observeIsOnline,
        // other dependencies...
    )
}

// In a test: emit a value
fakeIsOnlineChannel.trySend(false)

// In a test: make the flow throw
fakeIsOnlineChannel.close(DataError.Unexpected())
```

### 2.3 `tearDown()` method (@AfterEach)
- Mocks are recreated in `setup()` for every test, so `@AfterEach` is not needed by default
- Add it only when the test class uses `mockkObject` or `mockkStatic`, because those replace global state that leaks into other test classes
- Call `unmockkAll()` in it
- Changing the JVM default locale or time zone is allowed when the result under test depends on it (e.g. `Money.toCurrencyText`): save the original value and set the new one in `setup()`, and always restore the original in `@AfterEach`

```kotlin
@AfterEach
fun tearDown() {
    unmockkAll()
}
```

```kotlin
private lateinit var originalLocale: Locale

@BeforeEach
fun setup() {
    originalLocale = Locale.getDefault()
    Locale.setDefault(Locale.UK)
}

@AfterEach
fun tearDown() {
    Locale.setDefault(originalLocale)
}
```

## 3. Mocks and dependencies

### 3.1 Declaring mocks
- Declare mocks as `private lateinit var` properties of the test class and instantiate them in `setup()`
- Always use the strict `mockk()`, never `mockk(relaxed = true)` (see 9.4)

```kotlin
private lateinit var merchantRepository: MerchantRepository
private lateinit var deviceIdentityRepository: DeviceIdentityRepository

@BeforeEach
fun setup() {
    merchantRepository = mockk()
    deviceIdentityRepository = mockk()
}
```

### 3.2 Configuring mocks
- Configure mock behavior in the test method; when reused, prefer creating it inside the test methods to isolate each behavior
- Use `every { }` for synchronous functions
- Use `coEvery { }` for suspend functions
- Use `just runs` for methods that return no value

```kotlin
every { observeIsOnline() } returns flowOf(true)
coEvery { deviceIdentityRepository.getDeviceId() } returns Result.success(fakeDeviceId)
every { fakeEditor.apply() } just runs
```

### 3.3 `Result` return values
- Repositories and use cases return `Result<T>` instead of throwing, so a failure is configured with `Result.failure(...)` holding a `DataError`, never with `throws`
- Assert on the state or the returned `Result`. `assertThrows` stays for code that really throws (e.g. `MoneyConverter`)

```kotlin
coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

val fakeResult = repository.getBalance()

assertInstanceOf(DataError.Network::class.java, fakeResult.exceptionOrNull())
```

### 3.4 `SavedStateHandle`
- Use a real `SavedStateHandle`, never a mock: it works on the JVM, and it lets a test read back what the class saved or start it from saved values
- Two tests must never share the same instance: create a new one in `setup()`, as any other dependency, and never keep it in a companion object or a top-level property
- To test a class restored from saved values, use a `@Nested` class whose own `setup()` creates the handle with those values and creates the class under test again, because the class under test is never created inside a test method

```kotlin
private lateinit var savedStateHandle: SavedStateHandle

@BeforeEach
fun setup() {
    savedStateHandle = SavedStateHandle()
    // other dependencies, then create the class under test...
}

@Test
fun `if the amount changes should save it in the SavedStateHandle`() = runTest {
    // Given
    val fakeAmount = "400"
    every { isPayoutFormComplete(any(), any()) } returns false

    // When
    viewModel.onAmountChange(fakeAmount)

    // Then
    assertEquals(fakeAmount, savedStateHandle.get<String>("amount"))
}

@Nested
inner class RestoredFromSavedState {

    @BeforeEach
    fun setup() {
        savedStateHandle = SavedStateHandle(mapOf("amount" to "400"))
        viewModel = PayoutViewModel(
            savedStateHandle = savedStateHandle,
            // other dependencies...
        )
    }

    @Test
    fun `if an amount was saved should start the form with it`() = runTest {
        // Given
        val fakeSavedAmount = "400"

        // When
        val fakeState = viewModel.uiState.value

        // Then
        assertEquals(fakeSavedAmount, (fakeState as PayoutUiState.Form).amount)
    }
}
```

### 3.5 Use cases in ViewModel tests
- ViewModel tests always mock their use cases, even the ones with no dependencies (e.g. `ValidateIbanUseCase`). These tests validate the ViewModel's logic in isolation, and the real rules of the use cases are tested in the `:domain` test classes

## 4. Test structure

### 4.1 Test naming
- Use descriptive names with backticks, in English
- Follow the pattern: `` `if <condition> should <expected behavior>` ``
- Clearly describe the condition and the expected behavior

```kotlin
@Test
fun `if getActivity returns an empty page should emit Empty ui state`() = runTest {
    // ...
}

@Test
fun `if merchantApi throws an IOException should return a Network failure`() = runTest {
    // ...
}
```

### 4.2 Given / When / Then structure
- Organize the test into three sections, each one marked with its comment:
  - `// Given`: prepare data and configure mocks
  - `// When`: invoke the method under test
  - `// Then`: verify the results

```kotlin
@Test
fun `if getActivity fails should emit Error ui state`() = runTest {
    // Given
    coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

    // When
    viewModel.loadFirstPage()
    advanceUntilIdle()

    // Then
    assertEquals(TransactionsUiState.Error, viewModel.uiState.value)
}
```

### 4.3 Coroutine tests
- Always use `runTest` for tests that involve coroutines
- Import `kotlinx.coroutines.test.runTest`
- Coroutines launched in `viewModelScope` do not run until the scheduler advances: call `advanceUntilIdle()` after the `// When` call and before asserting
- `delay()` is skipped in virtual time, so debounced methods are tested with `advanceUntilIdle()` (run everything) or `advanceTimeBy()` (stop in the middle of the delay)

```kotlin
@Test
fun `if loadHome is called twice before the first load starts should fetch the balance only once`() = runTest {
    // Given
    coEvery { getBalance() } returns Result.failure(DataError.ServiceUnavailable())
    coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

    // When
    viewModel.loadHome()
    viewModel.loadHome()
    advanceUntilIdle()

    // Then
    coVerify(exactly = 1) { getBalance() }
}
```

### 4.4 Flow tests
- For a cold `Flow` returned by a method, collect it directly with `first()` or `toList()`
- For a `StateFlow`/`SharedFlow` exposed by a ViewModel, collect every emission into a list inside `backgroundScope` with `UnconfinedTestDispatcher(testScheduler)`, then assert on the list:

```kotlin
private fun <T> TestScope.collectStates(stateFlow: StateFlow<T>): List<T> {
    val states = mutableListOf<T>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        stateFlow.toList(states)
    }
    return states
}

@Test
fun `if getActivity returns an empty page should emit Empty ui state`() = runTest {
    // Given
    val fakeEmptyPage = ActivityPage(items = emptyList(), nextCursor = null, hasMore = false)
    coEvery { getActivity(any(), any()) } returns Result.success(fakeEmptyPage)

    // When
    val states = collectStates(viewModel.uiState)
    viewModel.loadFirstPage()
    advanceUntilIdle()

    // Then
    assertEquals(listOf(TransactionsUiState.Loading, TransactionsUiState.Empty), states)
}
```

### 4.5 Mapper functions next to a state
- Each state that has `mapTo...` functions or extensions producing it gets its own test file named `<StateName>Test.kt`, holding the tests of those functions. `mapToHomeUiState` is tested in `HomeUiStateTest.kt`, and the `mapToSectionState` that produces a `BalanceSectionState` in `BalanceSectionStateTest.kt`, even though both are declared in `HomeUiState.kt`
- When a mapper depends on the date or the time zone, pass fixed values (e.g. the `zone` and `today` of `mapToTransactionListItems`)

## 5. Test data

### 5.1 Naming fake data
- Use the `fake` prefix for test data (e.g. `fakeDeviceId`, `fakeIban`, `fakeAmount`)
- Use descriptive names that indicate the data's type and purpose
- Declare data at the start of the test method

```kotlin
val fakeDeviceId = "fake-device-id"
val fakeIban = "GB29NWBK60161331926819"
val fakeAmount = Money(minorUnits = 40_000, currency = Currency.GBP)
val fakeCursor = "fake-cursor"
```

### 5.2 Test values
- Use realistic values that are clearly identifiable as test data
- Avoid values that could be confused with real data
- Use distinct values across different tests to make debugging easier

```kotlin
// ✅ Good
val fakeIban = "GB29NWBK60161331926819" // the example IBAN from the README
val fakeDeviceId = "fake-device-id"

// ❌ Avoid
val iban = "GB94BARC10201530093459" // could be a real account
```

## 6. Verifications and assertions

### 6.1 Assertions
- Use the JUnit 5 assertions (`org.junit.jupiter.api.Assertions`) with direct imports; `kotlin.test` is not a dependency of this project
- Use `assertEquals` for equality checks
- Use `assertTrue` / `assertFalse` for boolean checks
- Use `assertNull` / `assertNotNull` for null/non-null checks
- Use `assertInstanceOf` for type checks
- Use `assertThrows` when a call is expected to throw, never `try`/`catch` (a `try`/`catch` test passes when nothing is thrown)
- Do not use Kotlin's `assert()`: it has no failure message and depends on JVM assertions being enabled
- Verify multiple properties of the result when relevant

```kotlin
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertInstanceOf
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.assertThrows

assertEquals(TransactionsUiState.Loading, states[0])
assertInstanceOf(HomeUiState.Content::class.java, states[1])
assertTrue(shouldRequestBiometric(fakeAmount))

assertThrows<ArithmeticException> {
    MoneyConverter.toMinorUnits(BigDecimal("10.999"), Currency.GBP)
}
```

### 6.2 Call verification
- Use `coVerify` to verify calls to suspend functions
- Use `verify` to verify calls to synchronous functions
- Always specify the exact number of expected calls with `exactly = N`
- Verify passed parameters when relevant

```kotlin
coVerify(exactly = 1) {
    getActivity(fakeCursor, fakeLimit)
}

verify(exactly = 1) {
    observeIsOnline()
}
```

### 6.3 Verifying non-calls
- Explicitly verify when methods should not be called using `exactly = 0` in `verify`/`coVerify`
- This makes the test's intent clearer

```kotlin
coVerify(exactly = 0) {
    merchantRepository.createPayout(any())
}
```

## 7. Test cases

### 7.1 Scenario coverage
- Test the happy path
- Test error cases and exceptions
- Test cases with null values
- Test cases with empty/default values
- Test edge cases (boundary values)

### 7.2 Common test-case examples

```kotlin
// Success case
@Test
fun `if getActivity returns a not empty page should emit Content ui state`() = runTest {
    // ...
}

// Empty value case
@Test
fun `if getActivity returns an empty page should emit Empty ui state`() = runTest {
    // ...
}

// Null value case
@Test
fun `if the page has no next cursor should not fetch another page`() = runTest {
    // Given
    val fakePage = ActivityPage(items = listOf(createFakeActivityItem()), nextCursor = null, hasMore = false)
    coEvery { getActivity(any(), any()) } returns Result.success(fakePage)

    // When
    viewModel.loadFirstPage()
    advanceUntilIdle()
    viewModel.loadNextPage()
    advanceUntilIdle()

    // Then
    coVerify(exactly = 1) { getActivity(any(), any()) }
}

// Error case
@Test
fun `if getActivity fails should emit Error ui state after Loading`() = runTest {
    // Given
    coEvery { getActivity(any(), any()) } returns Result.failure(DataError.ServiceUnavailable())

    // When
    val states = collectStates(viewModel.uiState)
    viewModel.loadFirstPage()
    advanceUntilIdle()

    // Then
    assertEquals(2, states.size)
    assertEquals(TransactionsUiState.Loading, states[0])
    assertInstanceOf(TransactionsUiState.Error::class.java, states[1])
}
```

## 8. Handling external dependencies

### 8.1 Android Context
- Use `mockk<Context>()` when needed

### 8.2 Dispatchers
- Never mock `CoroutineDispatcher`
- Pass `UnconfinedTestDispatcher()` to the classes that receive a dispatcher (e.g. the `ioDispatcher` of `DeviceIdentityRepositoryImpl`)
- Create it with no arguments and only in a class annotated with `@ExtendWith(MainDispatcherExtension::class)`, so it shares the scheduler used by `runTest` (see 1.1)

```kotlin
repository = DeviceIdentityRepositoryImpl(
    ioDispatcher = UnconfinedTestDispatcher(),
    // other dependencies...
)
```

## 9. Good practices

### 9.1 Isolation
- Each test must be independent and must not rely on other tests
- Recreate mocks and the class under test for every test in `setup()`
- Do not share state across tests

### 9.2 Clarity
- Tests must be easy to read and understand
- Use descriptive names for variables and methods
- Comment complex code when necessary
- Avoid complex logic in tests

### 9.3 Maintainability
- Extract common test data into helper methods when reused
- Use helper functions to build complex test objects

```kotlin
private fun createFakeActivityItem(
    id: String = "fake-activity-id",
    amount: Money = Money(minorUnits = 40_000, currency = Currency.GBP),
    description: String = "Fake payment",
): ActivityItem = ActivityItem(
    id = id,
    type = ActivityType.Deposit,
    amount = amount,
    date = Instant.parse("2026-01-23T10:00:00Z"),
    description = description,
    status = ActivityStatus.Completed,
)
```

### 9.4 Strict mocks
- Never use `relaxed = true` on mocks: it lets additions or modifications to the concrete logic go untested (it can produce a false positive — tests covering lines without exercising the new behavior)
- Add an `every`/`coEvery` for the methods and data used by the methods under test
- Configure only the behavior required by the test

### 9.5 Argument capture
- Use `slot` when you need to capture arguments passed to mocks (possible callbacks and lambdas)

```kotlin
val payoutRequestSlot = slot<PayoutRequest>()
coEvery { merchantRepository.createPayout(capture(payoutRequestSlot)) } returns Result.success(fakePayout)

// When ...

assertEquals(fakeDeviceId, payoutRequestSlot.captured.deviceId)
```

## 10. Review checklist

Before finalizing a test, verify:

- [ ] The class uses `@ExtendWith(MainDispatcherExtension::class)` if it involves coroutines
- [ ] The class has `@OptIn(ExperimentalCoroutinesApi::class)` if it uses experimental coroutine test APIs
- [ ] There is no dependency injection context in the tests
- [ ] Mocks are `lateinit var`s created with strict `mockk()` in `@BeforeEach`, with no `relaxed = true`
- [ ] The class under test is created in `setup()`, never inside a test method (except a plain data class that receives the fake values in its constructor)
- [ ] `SavedStateHandle` is real and created anew in `setup()`, never shared between tests
- [ ] ViewModel tests mock every use case, even the ones with no dependencies
- [ ] Failures are configured as `Result.failure(...)` when the dependency returns `Result`
- [ ] Mapper functions are tested in the `<StateName>Test.kt` of the state they produce
- [ ] Mock behaviors are configured inside the test methods, not in `setup()` (except the call the class under test makes when it is created)
- [ ] Has `@AfterEach` with `unmockkAll()` only if `mockkObject`/`mockkStatic` is used
- [ ] A changed default locale or time zone is restored in `@AfterEach`
- [ ] Test name follows `` `if <condition> should <expected behavior>` ``
- [ ] Test has the `// Given`, `// When`, `// Then` comments
- [ ] Test uses `runTest` if it involves coroutines, and advances the scheduler before asserting
- [ ] Test data uses the `fake` prefix and is descriptive
- [ ] Assertions come from `org.junit.jupiter.api.Assertions`, with no Kotlin `assert()` and no `try`/`catch`
- [ ] Calls are verified with `coVerify`/`verify` and `exactly = N`
- [ ] Tests success, error, null, and empty cases when applicable
- [ ] Code is clean and easy to understand

**Last updated:** 01/11/2026
