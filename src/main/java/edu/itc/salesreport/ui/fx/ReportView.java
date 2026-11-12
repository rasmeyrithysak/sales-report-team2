package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesEvent.BranchSelected;
import edu.itc.salesreport.ui.SalesEvent.MonthLoaded;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** MVC view: observes SalesModel, forwards gestures to the controller. */
public final class ReportView implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final BorderPane root = new BorderPane();
    private final TextField monthField = new TextField("2026-09");
    private final ComboBox<String> branchBox = new ComboBox<>();
    private final TableView<SaleTransaction> table = new TableView<>();
    private final BarChart<String, Number> chart =
            new BarChart<>(new CategoryAxis(), new NumberAxis());
    private final Label status = new Label("No month loaded");
    private final Label error = new Label();

    public ReportView(SalesModel model) {
        this.model = model;
        branchBox.getItems().setAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");
        table.getColumns().setAll(List.of(
                column("Date", t -> t.date().toString()),
                column("Receipt", SaleTransaction::receiptNo),
                column("Product", SaleTransaction::productName),
                column("Category", SaleTransaction::category),
                column("Qty", t -> Integer.toString(t.quantity())),
                column("Revenue", t -> t.revenue().toString())
        ));
        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        error.setStyle("-fx-text-fill: firebrick;");

        var filters = new HBox(8, new Label("Month"), monthField,
                new Label("Branch"), branchBox);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(8));
        var bottom = new VBox(4, status, error);
        bottom.setPadding(new Insets(8));
        root.setTop(filters);
        root.setCenter(new SplitPane(table, chart));
        root.setBottom(bottom);
        model.addListener(this);
    }

    public Parent root() { return root; }

    /** Gestures are forwarded as raw text; the controller decides. */
    public void bind(ReportController controller) {
        monthField.setOnAction(e -> controller.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> controller.branchChosen(branchBox.getValue()));
    }

    @Override
    public void modelChanged(SalesEvent event) {
        branchBox.setValue(model.branch());
        table.getItems().setAll(model.visibleTransactions());

        BranchSummary s = model.summary();
        var series = new XYChart.Series<String, Number>();
        s.revenueByCategory().forEach(
                (category, revenue) -> series.getData()
                        .add(new XYChart.Data<>(category, revenue)));
        chart.getData().setAll(List.of(series));

        String figures = "revenue USD %s · receipts %d"
                .formatted(s.revenue(), s.receipts());
        status.setText(switch (event) {
            case MonthLoaded(var month, var rows, var rejected) ->
                    "%s: %d rows, %d rejected · %s"
                            .formatted(month, rows, rejected, figures);
            case BranchSelected(var branch) -> branch + " · " + figures;
        });
    }

    @Override public void showError(String message) { error.setText(message); }
    @Override public void clearError() { error.setText(""); }

    // records have no JavaFX properties: adapt each column with a lambda
    private static TableColumn<SaleTransaction, String> column(String title,
            Function<SaleTransaction, String> value) {
        var col = new TableColumn<SaleTransaction, String>(title);
        col.setCellValueFactory(
                c -> new ReadOnlyStringWrapper(value.apply(c.getValue())));
        return col;
    }
}
