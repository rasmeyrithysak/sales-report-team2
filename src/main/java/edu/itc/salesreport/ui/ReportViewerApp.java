package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Composition root: the only class that knows every concrete type. */
public class ReportViewerApp extends Application {

    private ExecutorService executor;

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

        // Task 4: Background jobs powered by Java virtual threads
        executor = Executors.newVirtualThreadPerTaskExecutor();
        var vm = new ReportViewModel(source, new FxBackgroundJobs(executor),
                Clock.systemDefaultZone());

        stage.setScene(new Scene(new ReportView(vm).root(), 960, 600));
        stage.setTitle("Angkor Mart · Monthly report (MVVM)");
        stage.show();
        DashboardWindow.open(vm);
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}