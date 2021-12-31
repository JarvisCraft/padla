package ru.progrm_jarvis.padla.annotation.importing;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleImportsTest {

    @Test
    void importsWithoutCollisions() {
        val imports = SimpleImports.create("foo.bar", "Baz");
        val importsView = imports.imports();

        val expectedImports = new TreeSet<String>();
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ghi", imports.importClass("abc.def.Ghi"));
        expectedImports.add("abc.def.Ghi");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Areg", imports.importClass("tinkoff.invest", "Areg"));
        expectedImports.add("tinkoff.invest.Areg");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Shandurenko", imports.importClass("tinkoff.invest.Shandurenko"));
        expectedImports.add("tinkoff.invest.Shandurenko");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Alpha", imports.importClass("wolfram", "Alpha"));
        expectedImports.add("wolfram.Alpha");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Qux", imports.importClass("foo.bar.Qux"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Wow", imports.importClass("foo.bar", "Wow"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Yes", imports.importClass("o.ma.gad.Yes"));
        expectedImports.add("o.ma.gad.Yes");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("String", imports.importClass("java.lang.String"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());
        assertEquals(expectedImports, imports.imports());

        assertEquals("Class", imports.importClass("java.lang", "Class"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());
    }

    @Test
    void importsWithCollisions() {
        val imports = SimpleImports.create("foo.bar", "Baz");
        val importsView = imports.imports();

        val expectedImports = new TreeSet<String>();
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ghi", imports.importClass("abc.def.Ghi"));
        expectedImports.add("abc.def.Ghi");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Areg", imports.importClass("ru.vanillacraft.Areg"));
        expectedImports.add("ru.vanillacraft.Areg");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("tinkoff.invest.Areg", imports.importClass("tinkoff.invest.Areg"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("fake.Baz", imports.importClass("fake.Baz"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Ty", imports.importClass("q.w.e.r.Ty"));
        expectedImports.add("q.w.e.r.Ty");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("stop.shortening.Type.as.Ty", imports.importClass("stop.shortening.Type.as", "Ty"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Near", imports.importClass("foo.bar.Near"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("some.other.Near", imports.importClass("some.other.Near"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Faster", imports.importClass("i.am.Faster"));
        expectedImports.add("i.am.Faster");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("foo.bar.Faster", imports.importClass("foo.bar.Faster"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("String", imports.importClass("java.lang", "String"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("custom.String", imports.importClass("custom", "String"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("Class", imports.importClass("the.fastest.Class"));
        expectedImports.add("the.fastest.Class");
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());

        assertEquals("java.lang.Class", imports.importClass("java.lang.Class"));
        assertEquals(expectedImports, importsView);
        assertEquals(expectedImports, imports.imports());
    }
}