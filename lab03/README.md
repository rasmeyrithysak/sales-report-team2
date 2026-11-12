# Lab-03 · MVVM refactor of the Angkor Mart report viewer

Team repository: `sales-report-team2`, branch `lab-03`. Pull request: https://github.com/rasmeyrithysak/sales-report-team2/pull/1

## (a) Files of this lab

| Path | Purpose | Author |
|---|---|---|
| `src/main/java/edu/itc/salesreport/ui/Command.java`, `DelegateCommand.java` | Command abstraction with observable "can run" flag | rasmeyrithysak |
| `.../ui/SummarySource.java`, `BackgroundJobs.java`, `CallerThreadJobs.java` | Ports: model seen by the ViewModel, threading, synchronous baseline | rasmeyrithysak |
| `.../ui/ReportViewModel.java` | State, validation, commands, background load | rasmeyrithysak, menghong22 |
| `.../ui/ReportView.java`, `ReportViewerApp.java` | Bound view, composition root (dashboard window opened here) | rasmeyrithysak, menghong22, menhean0168-ctrl |
| `.../ui/FxBackgroundJobs.java`, `FrameGapMonitor.java`, `LoadBenchmarkApp.java` | Task-based jobs, responsiveness measurement | menghong22 |
| `src/test/.../ui/ManualJobs.java` | Test double: jobs run when the test says so | menhean0168-ctrl |
| `src/test/.../ui/ReportViewModelTest.java` | 15 toolkit-free ViewModel tests (incl. the two-views test) | menhean0168-ctrl |
| `src/test/.../ui/ViewModelImportsTest.java` | Guard: ViewModel imports javafx.base only | menhean0168-ctrl |
| `src/test/.../ui/UiIndependenceTest.java` | Lab-02 guard; `ui` removed from its list (ui now holds the view) | menhean0168-ctrl |
| `.../ui/DashboardView.java`, `DashboardWindow.java` | Challenge: second view on the same ViewModel | menhean0168-ctrl |
| `lab03/viewmodel-design.md`, `screenshots/viewer.png` | Tasks 1 to 3 design and screenshot | rasmeyrithysak, menghong22 |
| `lab03/measurements.md` | Task 4 measurements | Menghong22 |
| `lab03/mvc-vs-mvvm.md` | Task 5 comparison with numbers | menhean0168-ctrl |
| `lab03/platform-mapping.md` | Challenge: Jetpack / WPF mapping | menhean0168-ctrl |
| `lab03/screenshots/dashboard.png` | Challenge: viewer and dashboard side by side | menhean0168-ctrl |
| `lab03/README.md` | This file | menhean0168-ctrl |

## (b) Inputs reused from Lab-02

| Input | Source |
|---|---|
| Maven project, model (`edu.itc.salesreport.model`), CSV reader and parser, aggregator | tag `lab-02-final` = commit `2520c9c` (branch `origin/lab-02`) |
| Lab-02 MVC viewer (`ReportController`, `SalesModel`, `fx/ReportView`) | same tag, baseline for `mvc-vs-mvvm.md` |
| Sample data | generated with `SalesDataGenerator`, not committed |

## (c) Change log

| Date | What changed | Who | Model package changed? |
|---|---|---|---|
| 2026-11-12 | Task 5: 15 ViewModel tests, `ManualJobs`, `ViewModelImportsTest`, `mvc-vs-mvvm.md`; removed `ui` from `UiIndependenceTest` | menhean0168-ctrl | No |
| 2026-11-12 | Challenge: `DashboardView`, `DashboardWindow`, wired in `ReportViewerApp`, `platform-mapping.md`, `dashboard.png`; `ReportViewModel` not changed (`git diff lab-03-task5 -- .../ReportViewModel.java` is empty) | menhean0168-ctrl | No |

Check: `git diff --stat lab-02-final -- src/main/java/edu/itc/salesreport/model` prints nothing.
