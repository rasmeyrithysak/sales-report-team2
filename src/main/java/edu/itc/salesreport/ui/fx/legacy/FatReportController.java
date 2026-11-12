package edu.itc.salesreport.ui.fx.legacy;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import edu.itc.salesreport.ui.ReportController;

/** Refactored thin wrapper delegating to the proper MVC controller. */
public class FatReportController {
    private final TextField monthField;
    private final ComboBox<String> branchBox;
    private final Label status;
    private ReportController controller;

    public FatReportController(TextField monthField, ComboBox<String> branchBox, Label status) {
        this.monthField = monthField;
        this.branchBox = branchBox;
        this.status = status;
    }

    public void setController(ReportController controller) {
        this.controller = controller;
    }

    public void onLoad() {
        if (controller != null) {
            controller.monthEntered(monthField.getText());
        }
    }
}