package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.compiler.CompilerBackend;
import com.wynntils.templates.language.exception.LanguageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateLanguageTest {


    private TemplateLanguage language;
    @BeforeEach
    void setup() {
        language = new TemplateLanguage(new TemplateEngine(new CompilerBackend(this.getClass().getClassLoader())));
    }

    @Test
    void tokenizeBasic() {
        String input = "before {call(12;24)} after";

        List<TemplateLexer.Token> tokens = language.tokenize(input);

        assertEquals(11, tokens.size(), "Expected 11 tokens, got " + tokens.size());
    }

    @Test
    void tokenizeBasicStringNotInText() {
        String input = "before\"after\"";

        List<TemplateLexer.Token> tokens = language.tokenize(input);

        assertEquals(2, tokens.size(), "Expected 2 token, got " + tokens.size());
    }

    @Test
    void tokenizeDirective() {
        String input = "wassup#bro cool stuff\n#a: int = 4";

        List<TemplateLexer.Token> tokens = language.tokenize(input);

        assertEquals(4, tokens.size(), "Expected 4 token, got " + tokens.size());
    }

    @Test
    void tokenizeNesting() {
        String input = "{a(b(c(d(e(6;f(\"Hello world\"))))))}";

        List<TemplateLexer.Token> tokens = language.tokenize(input);

        assertEquals(24, tokens.size(), "Expected 24 token, got " + tokens.size());
    }

    @Test
    void parseBroken() {
        String input = "{)}";
        assertThrows(LanguageException.class, () -> language.parse(input));
    }

    @Test
    void parseBasic() {
        String input = "before {call(12;24)} after";

        Template template = language.parse(input);

        assertEquals(3, template.getParts().size());
    }

    @Test
    void parseNesting() {
        String input = "{a(b(c(d(e(6;f(\"Hello world\"))))))}";

        Template template = language.parse(input);

        assertEquals(1, template.getParts().size());
    }
}