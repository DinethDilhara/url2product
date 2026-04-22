package io.github.dinethdilhara.urltoproduct.model;

/**
 * Represents the result of a product extraction evaluation.
 *
 * <p>Contains a confidence score and the overall extraction status.</p>
 *
 * @param confidenceScore confidence score (0–100)
 * @param status extraction status of the result
 */
public record ExtractionResult(
        int confidenceScore,
        ExtractionStatus status
) {}