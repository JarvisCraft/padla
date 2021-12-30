package ru.progrm_jarvis.padla.annotation.importing;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleImportsTest {

    @Test
    void importsWithoutCollisions() {
        val imports = SimpleImports.create("foo.bar", "Baz");
        val importView = imports.imports();

        val expectedImports = new TreeSet<>();
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ghi", imports.importClass("abc.def.Ghi"));
        expectedImports.add("abc.def.Ghi");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Areg", imports.importClass("tinkoff.invest", "Areg"));
        expectedImports.add("tinkoff.invest.Areg");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Shandurenko", imports.importClass("tinkoff.invest.Shandurenko"));
        expectedImports.add("tinkoff.invest.Shandurenko");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Alpha", imports.importClass("wolfram", "Alpha"));
        expectedImports.add("wolfram.Alpha");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());
    }

    @Test
    void importsWithCollisions() {
        val imports = SimpleImports.create("foo.bar", "Baz");
        val importView = imports.imports();

        val expectedImports = new TreeSet<>();
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ghi", imports.importClass("abc.def.Ghi"));
        expectedImports.add("abc.def.Ghi");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Areg", imports.importClass("ru.vanillacraft.Areg"));
        expectedImports.add("ru.vanillacraft.Areg");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("tinkoff.invest.Areg", imports.importClass("tinkoff.invest.Areg"));
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("fake.Baz", imports.importClass("fake.Baz"));
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ty", imports.importClass("q.w.e.r.Ty"));
        expectedImports.add("q.w.e.r.Ty");
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("stop.shortening.Type.as.Ty", imports.importClass("stop.shortening.Type.as", "Ty"));
        assertEquals(expectedImports, importView);
        assertEquals(expectedImports, imports.imports());
    }
}