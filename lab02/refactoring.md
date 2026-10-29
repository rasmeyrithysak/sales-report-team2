# Refactoring Notes: Legacy Fat Controller (`FatReportController.java`)

This document identifies the architectural code smells present in the legacy `FatReportController` and records the metrics before and after refactoring it into a clean Model-View-Controller delegate.

---

## 1. Identified Code Smells & Consequences

1. **God Object / Fat Handler (Single Responsibility Principle Violation)**
   * **Smell:** The controller handles GUI event binding, filesystem I/O, file listing, CSV line splitting, numerical parsing, filtering logic, and business aggregation calculations all within a single `onLoad()` method.
   * **Consequence:** High maintenance overhead; any change to the CSV format or calculation rules forces modifications directly inside the UI presentation layer.

2. **Tight Coupling to File System and Concrete Implementation**
   * **Smell:** The class directly instantiates `Path.of("data", ...)` and invokes `Files.list()` and `Files.readAllLines()`.
   * **Consequence:** Impossible to test or run without a local file system configured in a very specific directory structure, and completely impossible to run on a machine without disk access or a display.

3. **Primitive Obsession & Magic Index Arrays**
   * **Smell:** CSV columns are extracted by splitting lines with commas (`line.split(",")`) and accessing raw array indices directly (e.g., `c[0]`, `c[6]`, `c[7]`, `c[8]`).
   * **Consequence:** Fragile code prone to `ArrayIndexOutOfBoundsException` or silent data corruption if column orders shift or optional fields vary.

4. **Silent Exception Swallowing & Misleading Error Reporting**
   * **Smell:** A broad `catch (Exception e)` block catches all errors and simply outputs `"Error"` to the status label.
   * **Consequence:** Users receive zero actionable feedback about *why* loading failed (e.g., whether a file was missing, a number was unparsable, or a date was malformed).

5. **Inconsistent and Unsafe Business Calculations**
   * **Smell:** The legacy controller computes line revenue on the fly using standard `BigDecimal` arithmetic mixed with raw strings, completely bypassing validation rules.
   * **Consequence:** When a broken or invalid row (like negative quantities) is present, it gets incorrectly summed into the total revenue instead of being rejected, leading to silent calculation discrepancies.

---

## 2. Behavior Comparison (`2026-09 ALL` with Broken Row Present)

| Metric / Scenario | Legacy Fat Controller (`FatReportController`) | Refactored Controller (`ReportController` + Model) |
| :--- | :--- | :--- |
| **Output for 2026-09 ALL (with broken row)** | `Revenue: 26715.40` (Wrong; silently includes bad row calculations) | `2026-09: 3600 rows, 1 rejected · revenue USD 26721.90` (Accurate; correctly rejects invalid row) |
| **Handling of Invalid Input (e.g., `2026/09`)** | Displays generic `"Error"` | Displays user-friendly validation message: `'2026/09' is not a month in the form yyyy-MM` |
| **Lines of Code in Handler** | ~20 lines of dense, coupled logic | 1 line (delegates cleanly to `ReportController`) |
| **Testability without JavaFX / Display** | 0 tests possible (tightly bound to UI controls and filesystem side effects) | Fully