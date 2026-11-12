package edu.itc.salesreport.ui.mvp;

/** One table row, already formatted for display. */
public record RowItem(String date, String receipt, String product,
        String category, String quantity, String revenue) {}
