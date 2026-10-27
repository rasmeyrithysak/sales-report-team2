package edu.itc.salesreport.ui;

/** Result of checking raw input: a usable value or a message for the user. */
public sealed interface Validation<T> {
    record Valid<T>(T value) implements Validation<T> {
    }

    record Invalid<T>(String message) implements Validation<T> {
    }
}
