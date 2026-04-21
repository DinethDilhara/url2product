package io.github.dinethdilhara.urltoproduct.util;

import io.github.dinethdilhara.urltoproduct.model.ExtractionStatus;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;

public class ExtractionEvaluator{

    public ExtractionStatus evaluateStatus(ProductDetails p) {

        int score = 0;

        // Title (critical)
        if (p.getTitle() != null && !p.getTitle().isBlank()) {
            score += 40;
        }

        // Price (critical)
        if (p.getPrice() != null) {
            score += 30;
        }

        // Description
        if (p.getDescription() != null && !p.getDescription().isBlank()) {
            score += 15;
        }

        // Images
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            score += 15;
        }

        if (score >= 80) {
            return ExtractionStatus.SUCCESS;
        } else if (score >= 40) {
            return ExtractionStatus.PARTIAL;
        } else {
            return ExtractionStatus.FAILED;
        }

    }

}
