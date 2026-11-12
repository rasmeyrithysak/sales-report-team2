# Lab-03 · MVC vs MVVM (own code)

MVC side = the Lab-02 viewer at `origin/lab-02` (commit `2520c9c`, to be tagged `lab-02-final`):
`fx/ReportView` + `fx/ReportApp` (view), `ReportController` + `InputRules` + `SalesModel` (+ small records/interfaces).
MVVM side = Lab-03: `ReportViewModel` + `ReportView` + ports/commands/jobs.

| Metric | MVC (Lab-02) | MVVM (Lab-03) | How measured |
|---|---|---|---|
| Lines of presentation code | 54 `ReportController` + 44 `InputRules` + 80 `SalesModel` + 47 small records/interfaces = 225 logic; 114 `fx/ReportView` + 34 `fx/ReportApp` = 148 view | 286 `ReportViewModel`; 137 `ReportView` (view only); 118 ports, commands, jobs (`Command` 15, `DelegateCommand` 30, `SummarySource` 10, `BackgroundJobs` 35, `CallerThreadJobs` 28) + 51 `FxBackgroundJobs` | `wc -l` |
| Presentation-logic line coverage without toolkit | 92.9 % (65 of 70 lines: `ReportController` 22/24, `InputRules` 15/15, `SalesModel` 28/31) | 96.5 % for `ReportViewModel` (109 of 113); 96.9 % with its inner listener and `Loaded` record (126 of 130) | JaCoCo (agent + report API), all tests run without a JavaFX toolkit |
| `javafx.scene` imports in the logic class | 0 (`ReportController`) | 0 (`ReportViewModel`) | `grep -c "^import javafx.scene"` |
| `javafx.scene` imports in the view class | 14 (`fx/ReportView`) | 14 (`ReportView`) | same |
| Control types the logic class references | 0 | 0 | code review |
| Test run time of presentation logic | 4 tests, 0.22 to 0.24 s | 15 tests, 0.25 to 0.32 s | JUnit console launcher XML, 3 runs each |
| Rules that live outside the view | month rule (`InputRules`) | month rule, branch rule, joined message, `canGenerate`, total text, filter, progress, error state | code review |
| UI frozen during a load | yes: `controller.monthEntered` calls `model.load` on the FX thread | no with `FxBackgroundJobs`: copy worst frame gap of both modes from `measurements.md` | Task 4 |

Test machine for these numbers: Intel Xeon 2.1 GHz, 1 vCPU, 3 GB, Ubuntu 24.04, OpenJDK 21, JavaFX 11 jars (base/controls/graphics), JUnit 5.10 console launcher, JaCoCo 0.8.11. The project targets JDK 25 / JavaFX 25.0.4 / JUnit 6.1.3 / JaCoCo 0.8.15: re-run `mvn verify` and read `target/site/jacoco/jacoco.csv` on your machine and replace the coverage and time cells if they differ.

## Conclusion

Our Lab-02 controller was already free of JavaFX types, so "can the logic run without the toolkit" does not separate the two designs the way the reference table suggests; the difference is how much of the screen the tested class owns. In MVC the controller only validates and forwards; the view still decides when buttons are enabled, formats the total, filters the product list and shows errors with `setText`. In MVVM all of this is state and rules in `ReportViewModel`, tested by 15 plain unit tests (validation, enablement, progress, failure, cancel, filter, thread snapshot) with a fixed clock and no sleeps. Moving the load behind `BackgroundJobs` also made threading testable: `ManualJobs` lets a test observe the "loading" state between `generate` and the result. The view shrank to creating controls and bindings, and a second view (`DashboardView`) reused the unchanged ViewModel. What got worse: more classes and interfaces (commands, ports, job listener), 286 lines in one ViewModel, and binding errors that show up at run time or as stale values rather than as compile errors, so debugging needs a listener or a test. I would still choose MVC (or plain MVP) for a small screen with two or three controls and no background work, where a ViewModel with commands and read-only wrappers is more machinery than the problem needs.
