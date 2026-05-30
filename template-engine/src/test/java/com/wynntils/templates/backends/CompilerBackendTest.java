/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.backends.compiler.CompilerBackend;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompilerBackendTest {
    private TemplateEngine engine;
    private CompilerBackend backend;

    @BeforeEach
    void setup() {
        backend = new CompilerBackend(this.getClass().getClassLoader());
        engine = new TemplateEngine(backend);
    }

    @Test
    void compileTestAdd() {
        String value = engine.evaluate("{add(1;1)}");

        Assertions.assertEquals("2.0", value);
    }

    @Test
    void compileTestConcat() {
        String value = engine.evaluate("{concat(\"Hello \";\"world\")}");

        Assertions.assertEquals("Hello world", value);
    }

    @Test
    void compileTestMulti() {
        String value = engine.evaluate("{4}x{4}={add(10;6)}\n{concat(\"Hello \";\"world\")}");

        Assertions.assertEquals("4.0x4.0=16.0\nHello world", value);
    }

    @Test
    void compileTestNested() {
        String value = engine.evaluate("{add(4;add(4; add(4;4;4)))}");

        Assertions.assertEquals("20.0", value);
    }

    @Test
    void compileTestAlias() {
        String value = engine.evaluate("{plus(4;2)}");

        Assertions.assertEquals("6.0", value);
    }
}
