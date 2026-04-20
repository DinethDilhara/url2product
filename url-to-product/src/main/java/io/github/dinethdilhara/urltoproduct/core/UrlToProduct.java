package io.github.dinethdilhara.urltoproduct.core;

import io.github.dinethdilhara.urltoproduct.engine.ProductResolver;
import io.github.dinethdilhara.urltoproduct.engine.ProviderLoader;
import io.github.dinethdilhara.urltoproduct.model.ProductDetails;

public class UrlToProduct {

    private final ProductResolver resolver;

    public UrlToProduct() {
        this.resolver = new ProductResolver(ProviderLoader.load());
    }

    public ProductDetails getData(String url) {
        return resolver.resolve(url).extract(url);
    }
}
