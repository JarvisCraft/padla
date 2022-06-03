package ru.progrm_jarvis.javacommons.delegate;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DelegateFactoryTests {

    private static @NotNull Stream<@NotNull Arguments> provideDelegateFactoryImplementations() {
        return Stream.of(ProxyDelegateFactory.create(), AsmDelegateFactory.create())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideDelegateFactoryImplementations")
    public void testDelegateWrapperOfInterface(final @NotNull DelegateFactory delegateFactory) {
        val implementation = mock(SimpleInterface.class);
        when(implementation.getInt()).thenReturn(0xCAFEBABE);
        when(implementation.toString(1)).thenReturn("1");
        when(implementation.toString(2)).thenReturn("2");
        when(implementation.toString(3)).thenReturn("3");

        @SuppressWarnings("unchecked") val factory = (Supplier<SimpleInterface>) mock(Supplier.class);
        when(factory.get()).thenReturn(implementation);

        val wrapper = delegateFactory.createWrapper(SimpleInterface.class, factory);

        // test that no unneeded calls are done
        verify(factory, times(0)).get();
        verify(implementation, times(0)).getInt();
        verify(implementation, times(0)).toString(anyInt());

        assertEquals(0xCAFEBABE, wrapper.getInt());
        verify(factory, times(1)).get();
        verify(implementation, times(1)).getInt();
        verify(implementation, times(0)).toString(anyInt());

        assertEquals("1", wrapper.toString(1));
        verify(factory, times(2)).get();
        verify(implementation, times(1)).getInt();
        verify(implementation, times(1)).toString(1);

        assertEquals("2", wrapper.toString(2));
        verify(factory, times(3)).get();
        verify(implementation, times(1)).getInt();
        verify(implementation, times(1)).toString(2);

        assertEquals("1", wrapper.toString(1));
        verify(factory, times(4)).get();
        verify(implementation, times(1)).getInt();
        verify(implementation, times(2)).toString(1);

        assertEquals(0xCAFEBABE, wrapper.getInt());
        verify(factory, times(5)).get();
        verify(implementation, times(2)).getInt();
        verify(implementation, times(3)).toString(anyInt());

        assertEquals("3", wrapper.toString(3));
        verify(factory, times(6)).get();
        verify(implementation, times(2)).getInt();
        verify(implementation, times(1)).toString(3);
    }

    @SuppressWarnings("InterfaceNeverImplemented")
    public interface SimpleInterface {

        int getInt();

        String toString(int number);
    }
}