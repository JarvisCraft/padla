import ru.progrm_jarvis.javacommons.delegate.AsmDelegateFactory;
import ru.progrm_jarvis.javacommons.delegate.DelegateFactory;
import ru.progrm_jarvis.javacommons.delegate.ProxyDelegateFactory;

module ru.progrm_jarvis.javacommons {
    // optional dependencies
    requires org.objectweb.asm;
    requires javassist;

    requires static lombok;
    requires static org.jetbrains.annotations;

    exports ru.progrm_jarvis.javacommons.annotation;
    exports ru.progrm_jarvis.javacommons.bytecode;
    exports ru.progrm_jarvis.javacommons.bytecode.annotation;
    exports ru.progrm_jarvis.javacommons.bytecode.asm;
    exports ru.progrm_jarvis.javacommons.cache;
    exports ru.progrm_jarvis.javacommons.classloading;
    exports ru.progrm_jarvis.javacommons.classloading.extension;
    exports ru.progrm_jarvis.javacommons.collection;
    exports ru.progrm_jarvis.javacommons.collection.concurrent;
    exports ru.progrm_jarvis.javacommons.data;
    exports ru.progrm_jarvis.javacommons.delegate;
    exports ru.progrm_jarvis.javacommons.invoke;
    exports ru.progrm_jarvis.javacommons.io.wrapper;
    exports ru.progrm_jarvis.javacommons.lazy;
    exports ru.progrm_jarvis.javacommons.object;
    exports ru.progrm_jarvis.javacommons.object.extension;
    exports ru.progrm_jarvis.javacommons.object.valuestorage;
    exports ru.progrm_jarvis.javacommons.ownership.annotation;
    exports ru.progrm_jarvis.javacommons.primitive;
    exports ru.progrm_jarvis.javacommons.primitive.error;
    exports ru.progrm_jarvis.javacommons.primitive.wrapper;
    exports ru.progrm_jarvis.javacommons.random;
    exports ru.progrm_jarvis.javacommons.range;
    exports ru.progrm_jarvis.javacommons.recursion;
    exports ru.progrm_jarvis.javacommons.service;
    exports ru.progrm_jarvis.javacommons.unsafe;
    exports ru.progrm_jarvis.javacommons.util;
    exports ru.progrm_jarvis.javacommons.util.concurrent;
    exports ru.progrm_jarvis.javacommons.util.function;
    exports ru.progrm_jarvis.javacommons.util.stream;
    exports ru.progrm_jarvis.javacommons.util.stream.extension;

    provides DelegateFactory with ProxyDelegateFactory, AsmDelegateFactory;
}
