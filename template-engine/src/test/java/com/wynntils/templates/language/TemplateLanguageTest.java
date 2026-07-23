/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.backends.compiler.CompilerBackend;
import com.wynntils.templates.language.exception.LanguageException;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateLanguageTest {
    private TemplateLanguage language;

    @BeforeEach
    void setup() {
        language = new TemplateLanguage(new TemplateEngine(new CompilerBackend(this.getClass().getClassLoader())));
    }

    @Test
    void tokenizeTest1() {
        assertTokens("{add(4;4)}", TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.ARGUMENTS_START, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.ARGUMENTS_END, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest2() {
        assertTokens("Hello {0} World", TemplateLexer.TokenType.TEXT, TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.TEXT, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest3() {
        assertTokens("{1.2;111.2356;1.7445;0.5;20;20000;10}", TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest4() {
        assertTokens("\"escape me \\\"\"{\"string\"}", TemplateLexer.TokenType.TEXT, TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.STRING, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest5() {
        assertTokens("{NAME; name; testname0123; doors}", TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest6() {
        Assertions.assertThrows(LanguageException.class, () -> language.tokenize("{123.4:3}"));
    }

    private static final char[] IDENTIFIER_CHARACTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    @Test
    void tokenizeGarbage() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < randomValue(10); i++) {
            result.append("{");
            generateFunctionCall(result, true);
            result.append("}");
            result.append("\n");
        }

        Assertions.assertDoesNotThrow(() -> language.tokenize(result.toString()));
    }


    private void generateFunctionCall(StringBuilder stringBuilder, boolean canGenerateFunctionCalls) {
        stringBuilder.append(generateIdentifier());
        stringBuilder.append("(");

        int count = randomValue(10);
        for (int i = 0; i < count; i++) {
            double rand = Math.random();
            if (rand < 0.2) {
                stringBuilder.append(generateIdentifier());
            } else if (rand < 0.4 && canGenerateFunctionCalls) {
                generateFunctionCall(stringBuilder, false);
            } else if(rand < 0.6) {
                stringBuilder.append("\"").append(generateIdentifier()).append("\"");
            }else {
                stringBuilder.append(Math.random() * 100);
            }

            if (i < count - 1) {
                stringBuilder.append(";");
            }
        }

        stringBuilder.append(")");
    }

    private int randomValue(int count) {
        return (int) (Math.random() * count) + 1;
    }

    private String generateIdentifier() {
        String identifier = "";
        for (int i = 0; i < randomValue(10); i++) {
            identifier += IDENTIFIER_CHARACTERS[(int) (Math.random() * IDENTIFIER_CHARACTERS.length - 1)];
        }
        return identifier;
    }

    void assertTokens(String input, TemplateLexer.TokenType... tokens) {
        List<TemplateLexer.Token> tokensList = language.tokenize(input);
        for (int i = 0; i < tokensList.size(); i++) {
            Assertions.assertEquals(tokens[i], tokensList.get(i).type());
        }
    }
}
