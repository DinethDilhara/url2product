package io.github.dinethdilhara.urltoproduct.core;

import io.github.dinethdilhara.urltoproduct.provider.ProductProvider;
import io.github.dinethdilhara.urltoproduct.util.ExtractionEvaluator;
import io.github.dinethdilhara.urltoproduct.exception.*;
import io.github.dinethdilhara.urltoproduct.engine.*;
import io.github.dinethdilhara.urltoproduct.model.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * UrlToProduct
 *
 * <p>A simple and extensible Java library for extracting structured product data
 * from e-commerce URLs.</p>
 *
 * <p>This class is the main entry point of the library. It automatically selects
 * the best available provider (Amazon, AliExpress, Generic fallback, etc.)
 * and returns normalized product data.</p>
 *
 * Example usage:
 * <pre>
 * UrlToProduct extractor = new UrlToProduct();
 * ProductDetails product = extractor.extract("https://example.com/product/123");
 * </pre>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public class UrlToProduct {

    private static final Logger log = LoggerFactory.getLogger(UrlToProduct.class);

    private final ProductResolver resolver;

    /**
     * Creates a new UrlToProduct extractor instance.
     *
     * <p>Internally loads all available product providers.</p>
     */
    public UrlToProduct() {
        this.resolver = new ProductResolver(ProviderLoader.load());
        log.debug("UrlToProduct initialized with {} providers", ProviderLoader.load().size());
    }

    /**
     * Extracts product information from the given URL.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Selects the best matching provider</li>
     *   <li>Extracts raw product data</li>
     *   <li>Evaluates confidence and status</li>
     *   <li>Returns normalized product result</li>
     * </ul>
     *
     * @param url product page URL (must be publicly accessible)
     * @return extracted product details with confidence confidenceScore and status
     * @throws UrlToProductException if extraction fails or URL is invalid
     */
    public ProductDetails extract(String url) {

        log.info("Starting product extraction for URL: {}", url);

        try {
            ProductProvider provider = resolver.resolve(url);

            log.debug("Selected provider: {}", provider.getClass().getSimpleName());

            ProductDetails details = provider.extract(url);

            ExtractionResult result = ExtractionEvaluator.evaluate(details);

            details.setStatus(result.status());
            details.setConfidenceScore(result.confidenceScore());

            log.info("Extraction completed for URL: {} (status={}, confidence={})",
                    url, result.status(), result.confidenceScore());

            return details;

        } catch (Exception e) {

            log.error("Product extraction failed for URL: {}", url, e);

            throw ExceptionMapper.toException(e, url);
        }
    }
}
