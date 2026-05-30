/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates;

import com.wynntils.templates.backends.TemplateBackend;
import com.wynntils.templates.backends.compiler.CompilerBackend;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateEngineTest {
    private TemplateEngine engine;

    @BeforeEach
    void setup() {
        TemplateBackend backend = new CompilerBackend(this.getClass().getClassLoader());
        engine = new TemplateEngine(backend);
    }

    @Test
    void testTemplateEngine() {
        Assertions.assertNotNull(engine);
    }
}
