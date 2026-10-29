package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvTransactionParserTest {

    private final CsvTransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesValidRowSuccessfully() throws InvalidRowException {
        String line = "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
        var tx = parser.parse(line);
        
        assertEquals("PNH", tx.branch());
        assertEquals(LocalDate.parse("2026-09-01"), tx.date());
        assertEquals("PNH-000001", tx.receiptNo());
        assertEquals(new BigDecimal("13.00"), tx.revenue());
        assertEquals(PaymentMethod.KHQR, tx.paymentMethod());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "UNKNOWN,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR",     // Unknown branch
        "PNH,bad-date,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR",            // Bad date format
        "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,0,6.50,0.00,KHQR",          // Quantity <= 0
        "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,20.00,KHQR",         // Discount > line amount
        "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,UnknownCategory,2,6.50,0.00,KHQR",  // Unknown category
        "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,UNKNOWN_PAY",   // Unknown payment
        "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00"                 // Missing fields
    })
    void rejectsInvalidRows(String invalidLine) {
        assertThrows(InvalidRowException.class, () -> 
            parser.parse(invalidLine)
        );
    }
}