package io.github.dinethdilhara.urltoproduct.engine;

import io.github.dinethdilhara.urltoproduct.provider.ProductProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ProviderLoader {

    public static List<ProductProvider> load() {
        ServiceLoader<ProductProvider> loader =
                ServiceLoader.load(ProductProvider.class);

        List<ProductProvider> providers = new ArrayList<>();
        loader.forEach(providers::add);

        return providers;
    }
}
