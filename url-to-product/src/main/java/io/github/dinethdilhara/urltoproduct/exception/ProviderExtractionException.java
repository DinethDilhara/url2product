package io.github.dinethdilhara.urltoproduct.exception;

/**
 * Thrown when a provider fails to extract product data.
 *
 * @version 1.0.0
 */
public class ProviderExtractionException extends RuntimeException {

    private final String provider;

    public ProviderExtractionException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
