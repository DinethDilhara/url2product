package io.github.dinethdilhara.urltoproduct.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlUtils {

    public static String extractText(Document doc, String[] selectors) {
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) {
                return el.text().trim();
            }
        }
        return "N/A";
    }

    public static String extractAttr(Document doc, String[] selectors, String attr) {
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && el.hasAttr(attr)) {
                return el.absUrl(attr);
            }
        }
        return "N/A";
    }

}