package io.github.dinethdilhara.urltoproduct.engine;

import io.github.dinethdilhara.urltoproduct.provider.ProductProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ProviderLoader
 *
 * <p>Loads all available {@link ProductProvider} implementations
 * using Java's {@link ServiceLoader} mechanism.</p>
 *
 * <p>This enables a pluggable architecture where new providers
 * (e.g. Amazon, AliExpress) can be added without modifying core code.</p>
 *
 * How it works:
 * <ul>
 *   <li>Scans classpath for registered providers</li>
 *   <li>Instantiates each provider</li>
 *   <li>Returns a list of available providers</li>
 * </ul>
 *
 * @version 1.0.0
 * @author Dineth Dilhara
 */
public class ProviderLoader {

    /**
     * Loads all ProductProvider implementations.
     *
     * @return list of discovered providers (may be empty if none found)
     */
    public static List<ProductProvider> load() {
        ServiceLoader<ProductProvider> loader =
                ServiceLoader.load(ProductProvider.class);

        List<ProductProvider> providers = new ArrayList<>();
        loader.forEach(providers::add);

        return providers;
    }
}
