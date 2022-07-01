import ru.progrm_jarvis.ultimatemessenger.format.model.AsmTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.JavassistTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.SimpleTextModelFactory;
import ru.progrm_jarvis.ultimatemessenger.format.model.TextModelFactory;

module ru.progrm_jarvis.ultimatemessenger {

    requires transitive ru.progrm_jarvis.javacommons;

    requires java.logging;
    // optional dependencies
    requires org.objectweb.asm;
    requires javassist;

    requires static lombok;
    requires static org.jetbrains.annotations;

    exports ru.progrm_jarvis.ultimatemessenger.format;
    exports ru.progrm_jarvis.ultimatemessenger.format.model;
    exports ru.progrm_jarvis.ultimatemessenger.format.placeholder;
    exports ru.progrm_jarvis.ultimatemessenger.format.util;
    exports ru.progrm_jarvis.ultimatemessenger.message;

    provides TextModelFactory with SimpleTextModelFactory, AsmTextModelFactory, JavassistTextModelFactory;
}
