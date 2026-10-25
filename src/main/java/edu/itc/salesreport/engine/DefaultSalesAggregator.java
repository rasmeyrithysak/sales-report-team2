package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

/** Sums revenue, discounts, receipts, categories, payments, top products. */
public final class DefaultSalesAggregator implements SalesAggregator {
    private static final int TOP_PRODUCTS = 10;

    @Override
    public BranchSummary summarize(String branch, List<SaleTransaction> rows) {
        BigDecimal revenue = BigDecimal.ZERO.setScale(2);
        BigDecimal discounts = BigDecimal.ZERO.setScale(2);
        var receipts = new HashSet<String>();
        var byCategory = new TreeMap<String, BigDecimal>();
        var byPayment = new EnumMap<PaymentMethod, BigDecimal>(PaymentMethod.class);
        var byProduct = new HashMap<String, ProductTotal>();

        for (var t : rows) {
            BigDecimal line = t.revenue();
            revenue = revenue.add(line);
            discounts = discounts.add(t.discount());
            receipts.add(t.receiptNo());
            byCategory.merge(t.category(), line, BigDecimal::add);
            byPayment.merge(t.paymentMethod(), line, BigDecimal::add);
            byProduct.merge(t.sku(),
                    new ProductTotal(t.sku(), t.productName(), line, t.quantity()),
                    (a, b) -> new ProductTotal(a.sku(), a.productName(),
                            a.revenue().add(b.revenue()),
                            a.quantity() + b.quantity()));
        }

        var top = byProduct.values().stream()
                .sorted(Comparator.comparing(ProductTotal::revenue).reversed()
                        .thenComparing(ProductTotal::sku))
                .limit(TOP_PRODUCTS)
                .toList();

        return new BranchSummary(branch, revenue, discounts, receipts.size(),
                byCategory, top, byPayment);
    }
}
