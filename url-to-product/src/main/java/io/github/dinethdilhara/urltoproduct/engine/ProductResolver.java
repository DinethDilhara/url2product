package io.github.dinethdilhara.urltoproduct.engine;

import io.github.dinethdilhara.urltoproduct.exception.UnsupportedUrlException;
import io.github.dinethdilhara.urltoproduct.provider.ProductProvider;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

/**
 * ProductResolver
 *
 * <p>Responsible for selecting the appropriate {@link ProductProvider}
 * based on the given URL.</p>
 *
 * <p>This class uses a simple strategy: it finds the first provider
 * that supports the given URL. If no provider matches, an
 * {@link UnsupportedUrlException} is thrown.</p>
 *
 * <h3>Example:</h3>
 * <pre>
 * ProductProvider provider = resolver.resolve("https://example.com/product/123");
 * </pre>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public record ProductResolver(List<ProductProvider> providers) {

    private static final Logger log = LoggerFactory.getLogger(ProductResolver.class);

    /**
     * Resolves the appropriate provider for the given URL.
     *
     * @param url product URL
     * @return matching ProductProvider
     * @throws UnsupportedUrlException if no provider supports the URL
     */
    public ProductProvider resolve(String url) {

        log.debug("Resolving provider for URL: {}", url);

        return providers.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No provider found for URL: {}", url);
                    return new UnsupportedUrlException(url);
                });
    }
}