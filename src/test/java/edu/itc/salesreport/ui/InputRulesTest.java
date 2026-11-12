package edu.itc.salesreport.ui;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class InputRulesTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"), 
            ZoneId.of("Asia/Phnom_Penh")
    );

    @Test
    void validatesTrimmedValidMonth() {
        var result = InputRules.month("  2026-08  ", fixedClock);
        assertInstanceOf(Validation.Valid.class, result);
        assertEquals(YearMonth.of(2026, 8), ((Validation.Valid<YearMonth>) result).value());
    }

    @Test
    void rejectsInvalidMonthFormats() {
        String[] invalidInputs = {"", "  ", "2026/09", "2026-9", "Sept", "2026-10"};
        for (String input : invalidInputs) {
            var result = InputRules.month(input, fixedClock);
            assertInstanceOf(Validation.Invalid.class, result);
        }
    }

    @Test
    void rejectsFutureMonth() {
        var result = InputRules.month("2026-10", fixedClock);
        assertInstanceOf(Validation.Invalid.class, result);
        assertEquals("2026-10 is in the future", ((Validation.Invalid<?>) result).message());
    }

    @Test
    void normalizesBranchInput() {
        var valid = InputRules.branch(" pnh ");
        assertInstanceOf(Validation.Valid.class, valid);
        assertEquals("PNH", ((Validation.Valid<String>) valid).value());

        var invalid = InputRules.branch("XYZ");
        assertInstanceOf(Validation.Invalid.class, invalid);
    }
}