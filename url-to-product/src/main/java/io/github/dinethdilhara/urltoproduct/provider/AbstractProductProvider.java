package io.github.dinethdilhara.urltoproduct.provider;

import io.github.dinethdilhara.urltoproduct.exception.ProviderExtractionException;
import io.github.dinethdilhara.urltoproduct.util.ExtractionEvaluator;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Set;
import java.math.BigDecimal;

public abstract class AbstractProductProvider implements ProductProvider {

    @Override
    public boolean supports(String url) {
        if (url == null) return false;
        try {
            return matchesHost(new URL(url).getHost());
        } catch (MalformedURLException e) {
            return false;
        }
    }

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
            product.setStatus(ExtractionEvaluator.evaluate(product).status());
            return product;
        } catch (Exception e) {
                throw new ProviderExtractionException(
                        providerName(),
                        "Failed to extract product data from " + url,
                        e
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

    protected Document connectTo(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
    }

}