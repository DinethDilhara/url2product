package io.github.dinethdilhara.urltoproduct.engine;

import io.github.dinethdilhara.urltoproduct.exception.UnsupportedUrlException;
import io.github.dinethdilhara.urltoproduct.provider.ProductProvider;

import java.util.List;

public class ProductResolver {

    private final List<ProductProvider> providers;

    public ProductResolver(List<ProductProvider> providers) {
        this.providers = providers;
    }

    public ProductProvider resolve(String url) {
        return providers.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() -> new UnsupportedUrlException("No provider found"));
    }
}
