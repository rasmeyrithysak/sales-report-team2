package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.nio.file.Path;
import java.time.Clock;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Composition root: the only class that knows every concrete type. */
public class ReportViewerApp extends Application {

    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed()
                .getOrDefault("data", "data"));
        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        SummarySource source = (month, branch) -> {
            var rows = reader.read(dataDir, month).rows().stream()
                    .filter(t -> t.branch().equals(branch))
                    .toList();
            return aggregator.summarize(branch, rows);
        };
        // Task 2: CallerThreadJobs (UI still freezes, like Lab-02)
        // Task 4: new FxBackgroundJobs(workers)
        var vm = new ReportViewModel(source, new CallerThreadJobs(),
                Clock.systemDefaultZone());
        stage.setScene(new Scene(new ReportView(vm).root(), 960, 600));
        stage.setTitle("Angkor Mart · Monthly report (MVVM)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}