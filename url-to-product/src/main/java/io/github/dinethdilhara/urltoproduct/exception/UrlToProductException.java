package io.github.dinethdilhara.urltoproduct.exception;

import io.github.dinethdilhara.urltoproduct.model.ErrorDetail;

/**
 * Main exception exposed by the library.
 *
 * <p>Wraps a structured {@link ErrorDetail} for consistent error handling.</p>
 *
 * @version 1.0.0
 */
public class UrlToProductException extends RuntimeException {

    private final ErrorDetail error;

    public UrlToProductException(ErrorDetail error) {
        super(error.message());
        this.error = error;
    }

    public ErrorDetail getError() {
        return error;
    }
}