/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.compiler.CompilerBackend;
import com.wynntils.templates.language.exception.LanguageException;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.parts.TemplateExpressionPart;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateLanguageTest
{
    private TemplateLanguage language;

    @BeforeEach
    void setup()
    {
        language = new TemplateLanguage(new TemplateEngine(new CompilerBackend(this.getClass().getClassLoader())));
    }

    @Test
    void tokenizeTest1()
    {
        assertTokens("{add(4;4)}", TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.IDENTIFIER, TemplateLexer.TokenType.ARGUMENTS_START, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.SEMICOLON, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.ARGUMENTS_END, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.EOF);
    }

    @Test
    void tokenizeTest2()
    {
        assertTokens("Hello {0} World", TemplateLexer.TokenType.TEXT, TemplateLexer.TokenType.TEMPLATE_START, TemplateLexer.TokenType.NUMBER, TemplateLexer.TokenType.TEMPLATE_END, TemplateLexer.TokenType.TEXT, TemplateLexer.TokenType.EOF);
    }

    private static char[] characters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '}', '(', ')', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ':', ';',};

    @Test
    void tokenizeGarbage()
    {
        String garbage = "";
        int max = (int) (Math.random() * 500f);
        for (int i = 0; i < max; i++)
        {
            garbage += characters[(int) (Math.random() * characters.length - 1)];
        }
        String finalGarbage = garbage;
        Assertions.assertThrows(LanguageException.class, () -> language.tokenize(finalGarbage));
    }

    void assertTokens(String input, TemplateLexer.TokenType... tokens)
    {
        List<TemplateLexer.Token> tokensList = language.tokenize(input);
        for (int i = 0; i < tokensList.size(); i++)
        {
            Assertions.assertEquals(tokens[i], tokensList.get(i).type());
        }
    }
}
