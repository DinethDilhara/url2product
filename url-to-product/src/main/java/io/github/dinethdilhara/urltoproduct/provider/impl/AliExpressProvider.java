package io.github.dinethdilhara.urltoproduct.provider.impl;

import io.github.dinethdilhara.urltoproduct.provider.AbstractProductProvider;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AliExpressProvider extends AbstractProductProvider {

    private static final String[] TITLE_SELECTORS = {
            "h1[data-pl=product-title]",
            "h1[class*=product-title]",
            "meta[property=og:title]"
    };

    private static final String[] DESCRIPTION_SELECTORS = {
            "div.detail-desc-decorate-richtext",
            "div[class*=detail-desc-decorate-richtext]"
    };

    private static final String[] PRICE_SELECTORS = {
            "span[class*=price-default--current]",
            "span[class*=price-default--current--F8OlYIo]",
            "div[class*=product-price-current] span",
            "meta[property=product:price:amount]"
    };

    private static final String[] IMAGE_SELECTORS = {
            ".pdp-info-left img",
            "div[class*=image-view-v2] img",
            "meta[property=og:image]"
    };

    @Override
    protected boolean matchesHost(String host) {
        return host.contains("aliexpress");
    }

    @Override
    protected String providerName() { return "AliExpress"; }

    @Override
    protected String extractTitle(Document doc) {
        String value = extractBySelectors(doc, TITLE_SELECTORS);
        return value.isBlank() ? normalizeWhitespace(doc.title()) : value;
    }

    @Override
    protected String extractDescription(Document doc) {
        for (String selector : DESCRIPTION_SELECTORS) {
            Element element = doc.selectFirst(selector);
            if (element == null) continue;

            String html = element.html()
                    .replace("<br>", "\n")
                    .replace("<br/>", "\n")
                    .replace("<br />", "\n");

            String text = normalizeWhitespace(Jsoup.parse(html).text()).replace(" \n ", "\n");
            if (!text.isBlank()) return text;
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

        // Also pull images embedded in the description block
        for (String selector : DESCRIPTION_SELECTORS) {
            Element description = doc.selectFirst(selector);
            if (description == null) continue;
            for (Element img : description.select("img")) {
                String src = normalizeImageUrl(firstNonBlank(img.absUrl("src"), img.attr("src")));
                if (src != null && !src.isBlank()) images.add(src);
            }
        }

        return images;
    }
}