# Mapping `ReportViewModel` to Android Jetpack and WPF

| ReportViewModel member | Android Jetpack | WPF (.NET) |
|---|---|---|
| `month` (two-way) | `MutableStateFlow<YearMonth?>` in a `ViewModel`; view calls `onMonthChanged(m)` | property with `INotifyPropertyChanged`, `{Binding Month, Mode=TwoWay}` |
| `branchSelected(b)` (two-way) | `MutableStateFlow<Set<String>>` or `Map<String, Boolean>` | `bool` properties or `ObservableCollection<BranchItem>` with `IsSelected` |
| `productFilter` (two-way) | `MutableStateFlow<String>` | `string` property, `UpdateSourceTrigger=PropertyChanged` |
| `summaries`, `visibleProducts` | `StateFlow<List<BranchSummary>>`, `combine(products, filter)` | `ObservableCollection<T>` plus `ICollectionView.Filter` |
| `loading`, `progress`, `status`, `loadError` | immutable `UiState` data class exposed as `StateFlow<UiState>` | read-only properties raising `PropertyChanged` |
| `monthError`, `branchError`, `validationMessage`, `canGenerate` | derived with `combine`/`map`, or fields of `UiState` | computed properties, or `INotifyDataErrorInfo` for validation |
| `totalRevenue`, `totalRevenueText` | `map { sum }` and `NumberFormat` in the ViewModel | computed property, `StringFormat` in the binding |
| `generate` command | `fun generate()` called by `onClick` | `ICommand` (`RelayCommand`/`AsyncRelayCommand`) with `CanExecute` |
| `cancel` command | `fun cancel()` calling `job.cancel()` | `ICommand` that calls `CancellationTokenSource.Cancel()` |
| `BackgroundJobs` | `viewModelScope.launch(Dispatchers.IO)` with `withContext(Dispatchers.Main)` for results | `async`/`await` with `Task.Run`, `IProgress<double>`, `CancellationToken` |
| `SummarySource` | repository interface injected into the ViewModel | service interface injected into the ViewModel |
| `Clock` | `java.time.Clock` or `kotlinx-datetime` `Clock` | `TimeProvider` / `IClock` |

## What would not carry over

Jetpack has no built-in two-way binding in Compose or Views, so `month`, `branchSelected` and `productFilter` become one-way state plus explicit `onXChanged` functions. A Jetpack `ViewModel` survives rotation and is cleared with its owner, while our ViewModel lives as long as the JavaFX app, so process death and `SavedStateHandle` need extra handling on Android. JavaFX `Task` delivers callbacks on the FX thread by itself, but coroutines need the right dispatcher chosen explicitly, and WPF needs `async`/`await` or `Dispatcher` for the same guarantee. Our read-only wrappers (`ReadOnlyBooleanWrapper`) have no direct equivalent: Kotlin uses `StateFlow` vs `MutableStateFlow`, WPF uses private setters. Finally, `javafx.beans` is our only dependency, so the ViewModel code itself must be rewritten in Kotlin or C#; what transfers is the design: inputs, outputs, computed state, commands and a threading port.
