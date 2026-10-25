package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;
import java.util.List;

/** Computes the report figures for one branch (or ALL) from raw rows. */
public interface SalesAggregator {
    BranchSummary summarize(String branch, List<SaleTransaction> rows);
}
