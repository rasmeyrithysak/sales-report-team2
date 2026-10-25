package edu.itc.salesreport.ui;

/** The only way a controller talks to the view. */
public interface ErrorDisplay {
    void showError(String message);

    void clearError();
}
