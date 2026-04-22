package io.github.dinethdilhara.urltoproduct.provider;

import io.github.dinethdilhara.urltoproduct.exception.ProviderExtractionException;
import io.github.dinethdilhara.urltoproduct.model.ExtractionResult;
import io.github.dinethdilhara.urltoproduct.util.ExtractionEvaluator;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Set;
import java.math.BigDecimal;


/**
 * Base implementation for product providers.
 *
 * <p>This class contains shared scraping logic used by all concrete providers
 * (e.g. Amazon, AliExpress, Generic).</p>
 *
 * <p>It provides common utilities such as:</p>
 * <ul>
 *   <li>HTML fetching using Jsoup</li>
 *   <li>Selector-based extraction helpers</li>
 *   <li>Price parsing and normalization</li>
 *   <li>Image URL cleaning</li>
 * </ul>
 *
 * <p>Concrete providers must implement site-specific logic such as:</p>
 * <ul>
 *   <li>{@link #matchesHost(String)}</li>
 *   <li>{@link #extractTitle(Document)}</li>
 *   <li>{@link #extractPrice(Document)}</li>
 * </ul>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public abstract class AbstractProductProvider implements ProductProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractProductProvider.class);

    @Override
    public boolean supports(String url) {
        if (url == null) return false;
        try {
            return matchesHost(new URL(url).getHost());
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Extracts product information from the given URL.
     *
     * <p>Fetches HTML, applies provider-specific parsing logic,
     * and returns normalized product data.</p>
     *
     * @param url product page URL
     * @return extracted {@link ProductDetails}
     * @throws ProviderExtractionException if scraping fails
     */
    @Override
    public ProductDetails extract(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .followRedirects(true)
                    .get();

            ProductDetails product = new ProductDetails();
            product.setLink(url);
            product.setTitle(extractTitle(doc));
            product.setDescription(extractDescription(doc));
            product.setPrice(extractPrice(doc));
            product.setImages(extractImages(doc));

            ExtractionResult result = ExtractionEvaluator.evaluate(product);
            product.setStatus(result.status());
            product.setConfidenceScore(result.confidenceScore());

            log.debug("Extraction successful | provider={} | url={} | score={} | status={}",
                    providerName(), url, result.confidenceScore(), result.status());

            return product;

        } catch (Exception exception) {

            log.error("Extraction failed | provider={} | url={}",
                    providerName(), url, exception);

            throw new ProviderExtractionException(
                    providerName(),
                    "Failed to extract product data from " + url,
                    exception
            );
        }
    }


    protected abstract boolean matchesHost(String host);

    protected abstract String providerName();

    protected abstract String extractTitle(Document doc);

    protected abstract String extractDescription(Document doc);

    protected abstract BigDecimal extractPrice(Document doc);

    protected abstract ArrayList<String> extractImages(Document doc);

    protected String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    protected String normalizeWhitespace(String value) {
        if (value == null) return "";
        return value
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s+", "\n")
                .trim();
    }

    protected String normalizeImageUrl(String url) {
        if (url == null || url.isBlank() || url.startsWith("data:image")) return null;
        int queryIndex = url.indexOf('?');
        return (queryIndex >= 0 ? url.substring(0, queryIndex) : url).trim();
    }

    /**
     * Converts raw price text into a normalized BigDecimal value.
     *
     * @param rawPrice raw price string from HTML
     * @return parsed price or null if invalid
     */
    protected BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) return null;

        String normalized = rawPrice
                .replace('\u00A0', ' ')
                .replaceAll("[^0-9.,]", "")
                .trim();

        if (normalized.isBlank()) return null;

        int lastComma = normalized.lastIndexOf(',');
        int lastDot = normalized.lastIndexOf('.');

        if (lastComma > lastDot) {
            normalized = normalized.replace(".", "").replace(',', '.');
        } else {
            normalized = normalized.replace(",", "");
        }

        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected String extractBySelectors(Document doc, String[] selectors) {
        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element == null) continue;

            String value = element.hasAttr("content")
                    ? element.attr("content")
                    : element.text();

            value = normalizeWhitespace(value);
            if (!value.isBlank()) return value;
        }
        return "";
    }

    protected BigDecimal extractPriceBySelectors(Document doc, String[] selectors) {
        for (String selector : selectors) {
            Element element = doc.selectFirst(selector);
            if (element == null) continue;

            String raw = element.hasAttr("content") ? element.attr("content")
                    : element.hasAttr("value") ? element.attr("value")
                    : element.text();

            BigDecimal parsed = parsePrice(raw);
            if (parsed != null) return parsed;
        }
        return null;
    }

    protected ArrayList<String> extractImagesBySelectors(Document doc, String[] selectors) {
        Set<String> images = new LinkedHashSet<>();
        for (String selector : selectors) {
            for (Element img : doc.select(selector)) {
                String src = firstNonBlank(
                        img.attr("data-old-hires"),
                        img.absUrl("data-src"),
                        img.absUrl("src"),
                        img.attr("data-src"),
                        img.attr("src"),
                        img.attr("content")
                );
                String normalized = normalizeImageUrl(src);
                if (normalized != null && !normalized.isBlank()) images.add(normalized);
            }
        }
        return new ArrayList<>(images);
    }
    /**
     * Creates a Jsoup connection for the given URL.
     *
     * @param url target URL
     * @return parsed HTML document
     * @throws Exception if connection fails
     */

    protected Document connectTo(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
    }

}