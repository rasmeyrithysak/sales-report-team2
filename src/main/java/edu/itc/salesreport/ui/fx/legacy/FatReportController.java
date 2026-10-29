// src/main/java/edu/itc/salesreport/ui/fx/legacy/FatReportController.java
package edu.itc.salesreport.ui.fx.legacy;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/** Smell on purpose: I/O, parsing, filtering and totals in a handler. */
public class FatReportController {
    private final TextField monthField;
    private final ComboBox<String> branchBox;
    private final Label status;

    public FatReportController(TextField monthField,
            ComboBox<String> branchBox, Label status) {
        this.monthField = monthField;
        this.branchBox = branchBox;
        this.status = status;
    }

    public void onLoad() {
        try {
            var dir = Path.of("data", monthField.getText());
            BigDecimal revenue = BigDecimal.ZERO;
            for (Path f : Files.list(dir).toList()) {
                var lines = Files.readAllLines(f);
                for (String line : lines.subList(1, lines.size())) {
                    String[] c = line.split(",");
                    if (!branchBox.getValue().equals("ALL")
                            && !c[0].equals(branchBox.getValue())) continue;
                    revenue = revenue.add(new BigDecimal(c[7])
                            .multiply(new BigDecimal(c[6]))
                            .subtract(new BigDecimal(c[8])));
                }
            }
            status.setText("Revenue: " + revenue);
        } catch (Exception e) {
            status.setText("Error");
        }
    }
}

// src/test/java/edu/itc/salesreport/ui/ReportControllerTest.java (skeleton)
/** No JavaFX here: the view is replaced by a recording fake. */
class ReportControllerTest {
    static final class FakeErrors implements ErrorDisplay {
        final List<String> shown = new ArrayList<>();
        @Override public void showError(String message) { shown.add(message); }
        @Override public void clearError() { shown.add("<cleared>"); }
    }

    @TempDir Path dataDir;
    private final FakeErrors errors = new FakeErrors();
    private final List<SalesEvent> events = new ArrayList<>();
    private SalesModel model;
    private ReportController controller;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        // TODO model + events::add listener + controller with a fixed clock
    }

    @Test
    void invalidMonthNeverReachesModel() {
        controller.monthEntered("09/2026");
        assertTrue(events.isEmpty());
        assertEquals(List.of("'09/2026' is not a month in the form yyyy-MM"),
                errors.shown);
    }
    // TODO validMonthLoadsModelAndClearsError, missingFolderBecomesMessage,
    //      branchFilterNarrowsRows
}