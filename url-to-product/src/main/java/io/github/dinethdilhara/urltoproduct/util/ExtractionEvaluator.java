package io.github.dinethdilhara.urltoproduct.util;

import io.github.dinethdilhara.urltoproduct.model.ExtractionResult;
import io.github.dinethdilhara.urltoproduct.model.ExtractionStatus;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;

/**
 * Evaluates the quality of extracted product data.
 *
 * <p>Provides scoring and status classification based on available fields
 * in {@link ProductDetails}.</p>
 *
 * <p>Scoring rules:</p>
 * <ul>
 *   <li>Title: 40 points</li>
 *   <li>Price: 30 points</li>
 *   <li>Description: 15 points</li>
 *   <li>Images: 15 points</li>
 * </ul>
 *
 * <p>Status is derived using {@link ExtractionStatus} thresholds:</p>
 * <ul>
 *   <li>≥ 80 → SUCCESS</li>
 *   <li>≥ 40 → PARTIAL</li>
 *   <li>&lt; 40 → FAILED</li>
 * </ul>
 *
 * @see ExtractionStatus
 * @see ExtractionResult
 * @see ProductDetails
 */
public class ExtractionEvaluator{

    public static ExtractionResult evaluate(ProductDetails p) {

        int score = 0;

        if (p.getTitle() != null && !p.getTitle().isBlank()) score += 40;
        if (p.getPrice() != null) score += 30;
        if (p.getDescription() != null && !p.getDescription().isBlank()) score += 15;
        if (p.getImages() != null && !p.getImages().isEmpty()) score += 15;

        ExtractionStatus status;

        if (score >= 80) {
            status = ExtractionStatus.SUCCESS;
        } else if (score >= 40) {
            status = ExtractionStatus.PARTIAL;
        } else {
            status = ExtractionStatus.FAILED;
        }

        return new ExtractionResult(score, status);
    }

}
