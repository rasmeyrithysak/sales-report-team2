package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Reads every branch file of one month through a TransactionParser. */
public final class CsvMonthReader {
    private static final Pattern FILE_NAME =
            Pattern.compile("(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv");

    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    /** @throws NoSuchFileException when dataDir/yyyy-MM does not exist */
    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path dir = dataDir.resolve(month.toString());
        if (!Files.isDirectory(dir)) {
            throw new NoSuchFileException(dir.toString());
        }
        List<Path> files;
        try (Stream<Path> listing = Files.list(dir)) {
            files = listing
                    .filter(p -> FILE_NAME.matcher(
                            p.getFileName().toString()).matches())
                    .sorted()
                    .toList();
        }
        var rows = new ArrayList<SaleTransaction>();
        var rejected = new ArrayList<String>();
        for (Path file : files) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {   // i = 0 is the header
                try {
                    rows.add(parser.parse(lines.get(i)));
                } catch (InvalidRowException e) {
                    rejected.add("%s:%d %s".formatted(
                            file.getFileName(), i + 1, e.getMessage()));
                }
            }
        }
        return new MonthData(month, rows, rejected);
    }
}
