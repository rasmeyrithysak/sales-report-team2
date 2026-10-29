package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.itc.salesreport.TestData;
import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.SalesEvent.BranchSelected;
import edu.itc.salesreport.ui.SalesEvent.MonthLoaded;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** No JavaFX here: the model is tested with a recording listener. */
class SalesModelTest {
    private static final YearMonth SEPTEMBER = YearMonth.parse("2026-09");

    @TempDir Path dataDir;
    private final List<SalesEvent> events = new ArrayList<>();
    private SalesModel model;

    @BeforeEach
    void setUp() throws IOException {
        TestData.writeSample(dataDir);
        model = new SalesModel(new CsvMonthReader(new CsvTransactionParser()),
                new DefaultSalesAggregator());
        model.addListener(events::add);
    }

    @Test
    void loadFiresMonthLoadedAndSumsChainRevenue() throws IOException {
        model.load(dataDir, SEPTEMBER);

        assertEquals(List.of(new MonthLoaded(SEPTEMBER, 3, 1)), events);
        assertEquals(new BigDecimal("52.00"), model.summary().revenue());
    }

    @Test
    void selectingSameBranchTwiceFiresOnce() throws IOException {
        model.load(dataDir, SEPTEMBER);
        events.clear();

        model.selectBranch("PNH");
        model.selectBranch("PNH");

        assertEquals(List.of(new BranchSelected("PNH")), events);
        assertEquals(new BigDecimal("31.00"), model.summary().revenue());
        assertEquals(new BigDecimal("31.00"), model.summary().averageBasket());
    }

    @Test
    void failedLoadKeepsOldDataAndFiresNothing() throws IOException {
        model.load(dataDir, SEPTEMBER);
        events.clear();

        assertThrows(NoSuchFileException.class,
                () -> model.load(dataDir, YearMonth.parse("2026-07")));

        assertTrue(events.isEmpty());
        assertEquals(3, model.visibleTransactions().size());
    }

    @Test
    void removedListenerIsNotCalled() throws IOException {
        var calls = new ArrayList<SalesEvent>();
        SalesModelListener extra = calls::add;
        model.addListener(extra);
        model.removeListener(extra);

        model.load(dataDir, SEPTEMBER);

        assertTrue(calls.isEmpty());
    }
}
