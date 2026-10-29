package edu.itc.salesreport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestData {
    public static void writeSample(Path dir) throws IOException {
        Path monthDir = dir.resolve("2026-09");
        Files.createDirectories(monthDir);
        
        Path csv = monthDir.resolve("PNH-2026-09-01.csv");
        Files.write(csv, List.of(
            "branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method",
            "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR"
        ));
    }
}