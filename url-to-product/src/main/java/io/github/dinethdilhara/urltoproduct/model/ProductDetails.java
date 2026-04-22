package io.github.dinethdilhara.urltoproduct.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents extracted product information.
 *
 * <p>This is the main output model returned by the library.</p>
 *
 * @version 1.0.0
 */
public class ProductDetails {

    /** Original product URL */
    private String link;
    private String title;
    private String description;
    private BigDecimal price;
    private List<String> images;

    private ExtractionStatus status;

    /** Confidence confidenceScore (0–100) */
    private int confidenceScore;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public ExtractionStatus getStatus() {
        return status;
    }

    public void setStatus(ExtractionStatus status) {
        this.status = status;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

}
