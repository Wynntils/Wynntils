package com.wynntils.templates;

import com.wynntils.templates.compiler.CompilerBackend;
import com.wynntils.templates.compiler.TemplateBackend;
import com.wynntils.templates.functions.TestFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateEngineTest {
     private TemplateEngine engine;

     @BeforeEach
     void setup() {
          TemplateBackend backend = new CompilerBackend(this.getClass().getClassLoader());
          engine = new TemplateEngine(backend);
     }
     @Test
     void testTemplateEngine() {
          assertNotNull(engine);
     }

     @Test
     void registerFunctions() {
          engine.registerFunctions(TestFunctions.class);

          assertEquals(1, engine.getFunctions().size());
     }
}