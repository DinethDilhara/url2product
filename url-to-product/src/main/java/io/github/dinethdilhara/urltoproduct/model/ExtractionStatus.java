package io.github.dinethdilhara.urltoproduct.model;

/**
 * Indicates the outcome of the extraction process.
 */
public enum ExtractionStatus {
    /** All key fields successfully extracted */
    SUCCESS,
    /** Some fields extracted, but incomplete */
    PARTIAL,
    /** Extraction failed or returned insufficient data */
    FAILED
}