package ru.progrm_jarvis.javacommons.delegate;

import lombok.*;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ru.progrm_jarvis.javacommons.object.ObjectUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Implementation of {@link DelegateFactory delegate factory} which uses runtime proxy generation via {@link Proxy}.
 */
public final class ProxyDelegateFactory implements DelegateFactory {

    /**
     * Creates a {@link Proxy}-based {@link DelegateFactory delegate factory}.
     *
     * @publicForSpi {@link #create() preferred creation method}
     */
    @ApiStatus.Internal
    @SuppressWarnings("PublicConstructor") // SPI API
    public ProxyDelegateFactory() {}

    /**
     * Creates a proxy-based {@link DelegateFactory delegate factory}.
     *
     * @return proxy-based delegate factory
     * @apiNote singleton may be used here
     */
    public static @NotNull DelegateFactory create() {
        return Singleton.INSTANCE;
    }

    @Override
    public <T> @NotNull T createWrapper(final @NonNull Class<T> targetType, final @NonNull Supplier<T> supplier) {
        val methodsByName = new HashMap<String, Map<ClassArrayWrapper, Method>>();
        for (val method : targetType.getDeclaredMethods()) {
            val previouslyAssociatedMethod = methodsByName
                    .computeIfAbsent(method.getName(), key -> new HashMap<>())
                    .put(new ClassArrayWrapper(method.getParameterTypes()), method);

            assert previouslyAssociatedMethod == null
                    : "there should be no previous method associated with the given name and type";
        }

        val objectProxy = Proxy.newProxyInstance(
                targetType.getClassLoader(),
                new Class[]{targetType},
                (proxy, method, arguments) -> {
                    if (arguments == null) arguments = ObjectUtil.EMPTY_ARRAY;

                    return methodsByName
                            .get(method.getName())
                            .get(new ClassArrayWrapper(method.getParameterTypes()))
                            .invoke(supplier.get(), arguments);
                }
        );

        assert targetType.isInstance(objectProxy) : "target type should be assignable from the proxy instance";

        @SuppressWarnings("unchecked")
        val specificProxy = (T) objectProxy;
        return specificProxy;
    }

    @Value
    private class ClassArrayWrapper {
        Class<?> @NotNull [] array;
    }

    @UtilityClass
    private static class Singleton {

        /**
         * Instance of {@link AsmDelegateFactory ASM-based delegate factory}
         */
        private final @NotNull DelegateFactory INSTANCE = new ProxyDelegateFactory();
    }
}
