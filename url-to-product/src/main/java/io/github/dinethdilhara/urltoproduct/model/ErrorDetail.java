package io.github.dinethdilhara.urltoproduct.model;

/**
 * Represents a standardized error response.
 *
 * @param type    error category (SYSTEM / NETWORK / PROVIDER)
 * @param message human-readable message
 * @param code    error code
 *
 */
public record ErrorDetail(
        String type,
        String message,
        String code
) {}