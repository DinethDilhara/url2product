package io.github.dinethdilhara.urltoproduct.provider.impl;

import io.github.dinethdilhara.urltoproduct.exception.ProviderExtractionException;
import io.github.dinethdilhara.urltoproduct.model.ExtractionResult;
import io.github.dinethdilhara.urltoproduct.provider.AbstractProductProvider;
import io.github.dinethdilhara.urltoproduct.util.ExtractionEvaluator;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Set;

/**
 * Fallback product provider.
 *
 * <p>This provider attempts to extract product data from any website using:</p>
 * <ul>
 *   <li>JSON-LD structured data (primary source)</li>
 *   <li>OpenGraph / meta tags (fallback)</li>
 *   <li>DOM selectors (final fallback)</li>
 * </ul>
 *
 * <p>Used when no specific provider (e.g. Amazon, AliExpress) matches the URL.</p>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */

public class GenericProvider extends AbstractProductProvider {

    private static final Logger log = LoggerFactory.getLogger(GenericProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String[] TITLE_SELECTORS = {
            "meta[property=og:title]",
            "meta[name=twitter:title]",
            "h1",
            "title"
    };

    private static final String[] DESCRIPTION_SELECTORS = {
            "meta[property=og:description]",
            "meta[name=twitter:description]",
            "meta[name=description]"
    };

    private static final String[] PRICE_SELECTORS = {
            "meta[property=product:price:amount]",
            "meta[property=og:price:amount]",
            "meta[property=product:price]",
            "[itemprop=price]",
            ".price",
            "#price"
    };

    private static final String[] IMAGE_SELECTORS = {
            "meta[property=og:image]",
            "meta[name=twitter:image]",
            "link[rel=image_src]",
            "meta[property=og:image:secure_url]"
    };

    @Override
    protected boolean matchesHost(String host) {
        return true; // fallback provider — supports everything
    }

    @Override
    protected String providerName() { return "Generic"; }

    /**
     * Overrides the base extract() to layer JSON-LD extraction first,
     * then fills any missing fields using the standard DOM selectors.
     */
    @Override
    public ProductDetails extract(String url) {
        try {
            log.debug("GenericProvider started extraction | url={}", url);

            Document doc = connectTo(url);

            ProductDetails product = extractFromJsonLd(doc);
            if (product == null) product = new ProductDetails();

            product.setLink(url);

            if (isBlank(product.getTitle()))
                product.setTitle(extractTitle(doc));

            if (isBlank(product.getDescription()) || "N/A".equals(product.getDescription()))
                product.setDescription(extractDescription(doc));

            if (product.getPrice() == null)
                product.setPrice(extractPrice(doc));

            if (product.getImages() == null || product.getImages().isEmpty())
                product.setImages(extractImages(doc));

            ExtractionResult result = ExtractionEvaluator.evaluate(product);
            product.setStatus(result.status());
            product.setConfidenceScore(result.confidenceScore());

            log.debug("GenericProvider extraction complete | url={} | score={} | status={}",
                    url, result.confidenceScore(), result.status());

            return product;

        } catch (Exception e) {

            log.error("GenericProvider extraction failed | url={}", url, e);

            throw new ProviderExtractionException(
                    providerName(),
                    "Failed to extract product data from " + url,
                    e
            );
        }
    }

    @Override
    protected String extractTitle(Document doc) {
        String value = extractBySelectors(doc, TITLE_SELECTORS);
        return value.isBlank() ? normalizeWhitespace(doc.title()) : value;
    }

    @Override
    protected String extractDescription(Document doc) {
        String value = extractBySelectors(doc, DESCRIPTION_SELECTORS);
        return value.isBlank() ? "N/A" : value;
    }

    @Override
    protected BigDecimal extractPrice(Document doc) {
        return extractPriceBySelectors(doc, PRICE_SELECTORS);
    }

    @Override
    protected ArrayList<String> extractImages(Document doc) {
        Set<String> images = new LinkedHashSet<>();
        for (String selector : IMAGE_SELECTORS) {
            for (Element element : doc.select(selector)) {
                String src = firstNonBlank(
                        element.attr("content"),
                        element.attr("href"),
                        element.attr("src")
                );
                String normalized = normalizeImageUrl(src);
                if (normalized != null && !normalized.isBlank()) images.add(normalized);
            }
        }
        return new ArrayList<>(images);
    }

    // JSON-LD extraction
    private ProductDetails extractFromJsonLd(Document doc) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(script.html());
                ProductDetails p = root.isArray()
                        ? parseArrayNode(root)
                        : parseProductNode(root);
                if (p != null) return p;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private ProductDetails parseArrayNode(JsonNode array) {
        for (JsonNode node : array) {
            ProductDetails p = parseProductNode(node);
            if (p != null) return p;
        }
        return null;
    }

    private ProductDetails parseProductNode(JsonNode node) {
        // Handle @graph (common in Yoast/WordPress)
        if (node.has("@graph")) {
            for (JsonNode gNode : node.get("@graph")) {
                ProductDetails p = parseProductNode(gNode);
                if (p != null) return p;
            }
        }

        if (!"Product".equalsIgnoreCase(node.path("@type").asText())) return null;

        ProductDetails data = new ProductDetails();
        data.setTitle(node.path("name").asText(null));
        data.setDescription(node.path("description").asText(null));
        data.setPrice(parsePriceFromOffers(node.path("offers")));
        data.setImages(parseImagesFromNode(node.path("image")));
        return data;
    }

    private BigDecimal parsePriceFromOffers(JsonNode offers) {
        if (offers.isMissingNode()) return null;
        JsonNode offer = offers.isArray() && offers.size() > 0 ? offers.get(0) : offers;
        return parsePrice(offer.path("price").asText(null));
    }

    private ArrayList<String> parseImagesFromNode(JsonNode imageNode) {
        ArrayList<String> images = new ArrayList<>();
        if (imageNode.isMissingNode()) return images;

        if (imageNode.isArray()) {
            for (JsonNode img : imageNode) {
                images.add(img.isObject() ? img.path("url").asText() : img.asText());
            }
        } else {
            images.add(imageNode.isObject() ? imageNode.path("url").asText() : imageNode.asText());
        }

        return images;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
