package io.shehzeb.platform.order.domain;

/** Raised when an operation is not valid for the order's current status. */
public class IllegalOrderStateException extends RuntimeException {
    public IllegalOrderStateException(String message) {
        super(message);
    }
}
