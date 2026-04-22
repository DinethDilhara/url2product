package io.github.dinethdilhara.urltoproduct.exception;

import io.github.dinethdilhara.urltoproduct.model.ErrorDetail;

import java.util.concurrent.TimeoutException;

/**
 * Maps internal exceptions to {@link UrlToProductException}.
 *
 * <p>Provides consistent error responses for consumers of the library.</p>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public final class ExceptionMapper {

    private ExceptionMapper() {}

    /**
     * Converts any exception into a standardized {@link UrlToProductException}.
     *
     * @param e   original exception
     * @param url input URL
     * @return mapped exception
     */
    public static UrlToProductException toException(Exception e, String url) {

        if (e instanceof UnsupportedUrlException ex) {
            return new UrlToProductException(
                    new ErrorDetail(
                            "PROVIDER_ERROR",
                            "No provider found for URL: " + ex.getUrl(),
                            "NO_PROVIDER"
                    )
            );
        }

        if (e instanceof ProviderExtractionException ex) {
            return new UrlToProductException(
                    new ErrorDetail(
                            "PROVIDER_ERROR",
                            ex.getProvider() + " failed to extract product data",
                            "PROVIDER_EXTRACTION_FAILED"
                    )
            );
        }

        if (e instanceof TimeoutException) {
            return new UrlToProductException(
                    new ErrorDetail(
                            "NETWORK_ERROR",
                            "Request timed out",
                            "TIMEOUT"
                    )
            );
        }

        return new UrlToProductException(
                new ErrorDetail(
                        "SYSTEM_ERROR",
                        "Unexpected extraction failure for URL: " + url,
                        "EXTRACTION_FAILED"
                )
        );
    }
}