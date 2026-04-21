package io.github.dinethdilhara.urltoproduct.provider;

import io.github.dinethdilhara.urltoproduct.model.ProductDetails;

public interface ProductProvider {

    boolean supports(String url);

    ProductDetails extract(String url);
}
