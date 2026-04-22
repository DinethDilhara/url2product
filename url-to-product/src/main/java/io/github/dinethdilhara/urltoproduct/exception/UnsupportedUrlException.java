package io.github.dinethdilhara.urltoproduct.exception;

/**
 * Thrown when no provider supports the given URL.
 *
 * @version 1.0.0
 */
public class UnsupportedUrlException extends RuntimeException {

    private final String url;

    public UnsupportedUrlException(String url) {
        super("No provider found for URL: " + url);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}