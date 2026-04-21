package io.github.dinethdilhara.urltoproduct.provider.impl;

import io.github.dinethdilhara.urltoproduct.provider.AbstractProductProvider;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AmazonProvider extends AbstractProductProvider {

    private static final String[] TITLE_SELECTORS = {
            "#productTitle",
            "#title #productTitle",
            "meta[property=og:title]"
    };

    private static final String[][] DESCRIPTION_SELECTORS = {
            {"#feature-bullets", "li .a-list-item"},
            {"#featurebullets_feature_div", "li .a-list-item"},
            {"#productOverview_feature_div", "tr"},
            {"#productDetails_detailBullets_sections1", "tr"}
    };

    private static final String[] PRICE_SELECTORS = {
            "#corePrice_feature_div .a-price .a-offscreen",
            "#corePriceDisplay_desktop_feature_div .a-price .a-offscreen",
            "#apex_offerDisplay_desktop .a-price .a-offscreen",
            "input[name='items[0.base][customerVisiblePrice][displayString]']",
            "meta[property='product:price:amount']"
    };

    private static final String[] IMAGE_SELECTORS = {
            "#altImages img",
            "#imageBlock img",
            "#main-image-container img",
            "#landingImage"
    };

    @Override
    protected boolean matchesHost(String host) {
        return host.contains("amazon.") || host.equals("a.co");
    }

    @Override
    protected String providerName() { return "Amazon"; }

    @Override
    protected String extractTitle(Document doc) {
        String value = extractBySelectors(doc, TITLE_SELECTORS);
        return value.isBlank() ? normalizeWhitespace(doc.title()) : value;
    }

    @Override
    protected String extractDescription(Document doc) {
        List<String> parts = new ArrayList<>();

        for (String[] pair : DESCRIPTION_SELECTORS) {
            Element container = doc.selectFirst(pair[0]);
            if (container == null) continue;

            for (Element item : container.select(pair[1])) {
                if ("tr".equals(pair[1])) {
                    String key = normalizeWhitespace(item.select("th, td.a-span3 span").text());
                    String val = normalizeWhitespace(item.select("td, td.a-span9 span").text());
                    if (!key.isBlank() && !val.isBlank() && !key.equals(val))
                        parts.add(key + ": " + val);
                } else {
                    String text = normalizeWhitespace(item.text());
                    if (!text.isBlank()) parts.add(text);
                }
            }

            if (!parts.isEmpty()) return String.join("\n", parts);
        }

        Element meta = doc.selectFirst("meta[name=description], meta[property=og:description]");
        if (meta != null) {
            String content = normalizeWhitespace(meta.attr("content"));
            if (!content.isBlank()) return content;
        }

        return "N/A";
    }

    @Override
    protected BigDecimal extractPrice(Document doc) {
        return extractPriceBySelectors(doc, PRICE_SELECTORS);
    }

    @Override
    protected ArrayList<String> extractImages(Document doc) {
        ArrayList<String> images = extractImagesBySelectors(doc, IMAGE_SELECTORS);

        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String normalized = normalizeImageUrl(ogImage.attr("content"));
            if (normalized != null) images.add(normalized);
        }

        return images;
    }
}
