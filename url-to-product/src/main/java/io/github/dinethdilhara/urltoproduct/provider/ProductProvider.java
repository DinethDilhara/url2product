package io.github.dinethdilhara.urltoproduct.provider;

import io.github.dinethdilhara.urltoproduct.model.ProductDetails;

/**
 * Contract for all product extraction providers.
 *
 * <p>Each implementation is responsible for extracting product data
 * from a specific type of website (e.g. Amazon, AliExpress, etc.).</p>
 *
 * <p>Providers must be stateless and thread-safe.</p>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public interface ProductProvider {

    /**
     * Checks whether this provider can handle the given URL.
     *
     * @param url product page URL
     * @return true if provider supports the URL, false otherwise
     */
    boolean supports(String url);

    /**
     * Extracts product information from the given URL.
     *
     * @param url product page URL
     * @return extracted {@link ProductDetails}
     */
    ProductDetails extract(String url);
}
