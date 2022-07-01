module ru.progrm_jarvis.reflector {
    requires transitive ru.progrm_jarvis.javacommons;

    requires static lombok;
    requires static org.jetbrains.annotations;

    exports ru.progrm_jarvis.reflector.wrapper;
    exports ru.progrm_jarvis.reflector.wrapper.invoke;
}
