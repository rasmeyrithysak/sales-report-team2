package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.stage.Stage;

/** Benchmark harness for Lab-03 Task 4. */
public final class LoadBenchmarkApp extends Application {

    @Override
    public void start(Stage stage) {
        var params = getParameters().getNamed();
        Path dataDir = Path.of(params.getOrDefault("data", "data"));
        YearMonth month = YearMonth.parse(params.getOrDefault("month", "2026-08"));

        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        SummarySource source = (m, branch) -> {
            var rows = reader.read(dataDir, m).rows().stream()
                    .filter(t -> t.branch().equals(branch))
                    .toList();
            return aggregator.summarize(branch, rows);
        };

        System.out.println("Running benchmark for " + month + " using data from " + dataDir);
        
        // Run CallerThreadJobs benchmark
        runBenchmark("caller-thread", new ReportViewModel(source, new CallerThreadJobs(), Clock.systemDefaultZone()), month);

        // Run FxBackgroundJobs benchmark
        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();
        runBenchmark("fx-task", new ReportViewModel(source, new FxBackgroundJobs(exec), Clock.systemDefaultZone()), month);
        exec.shutdown();

        System.exit(0);
    }

    private void runBenchmark(String mode, ReportViewModel vm, YearMonth month) {
        vm.monthProperty().set(month);
        FrameGapMonitor monitor = new FrameGapMonitor();
        monitor.start();

        for (int run = 1; run <= 5; run++) {
            long start = System.currentTimeMillis();
            monitor.reset();

            vm.generateCommand().execute();

            // Wait until loading finishes
            while (vm.loadingProperty().get()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            long duration = System.currentTimeMillis() - start;
            System.out.printf("%-13s  run %d  load ms %6d  max frame gap ms %6.0f%n",
                    mode, run, duration, monitor.maxGapMillis());
        }
        monitor.stop();
        System.out.println();
    }

    public static void main(String[] args) {
        launch(args);
    }
}