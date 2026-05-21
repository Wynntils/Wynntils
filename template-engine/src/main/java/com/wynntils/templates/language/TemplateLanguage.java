/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.language.exception.LanguageException;
import com.wynntils.templates.language.exception.LexException;
import com.wynntils.templates.language.exception.ParseException;
import com.wynntils.templates.language.exception.VerificationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateLanguage {
    private final Map<String, Template> templateCache = new HashMap<>();
    private final TemplateLexer lexer;
    private final TemplateParser parser;
    private final TemplateVerifier verifier;

    public TemplateLanguage(TemplateEngine engine) {
        this.parser = new TemplateParser(engine);
        this.verifier = new TemplateVerifier();
        this.lexer = new TemplateLexer();
    }

    public List<TemplateLexer.Token> tokenize(String input) {
        return lexer.tokenize(input);
    }

    public Template parse(String input) {
        return templateCache.computeIfAbsent(input, (str) -> verifier.verify(parser.parse(tokenize(str))));
    }

    public Template parseUnverified(String input) {
        return templateCache.computeIfAbsent(input, (str) -> parser.parse(tokenize(str)));
    }


    public String formatError(String input, LanguageException exception) {
        if (exception instanceof VerificationException verificationException) {
            return """
                VerificationException: %s
                """.formatted(verificationException.getMessage());
        }

        List<String> lines = input.lines().toList();

        int position = switch (exception) {
            case ParseException parse -> parse.getPosition();
            case LexException lex -> lex.getPosition();
            default -> -1;
        };

        int row = 0;
        int column = 0;
        int currentPosition = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineEnd = currentPosition + line.length();

            if (position <= lineEnd) {
                row = i;
                column = position - currentPosition;
                break;
            }

            currentPosition = lineEnd + 1; // newline
        }

        String previousLine = row > 0
                ? "%d: %s%n".formatted(row, lines.get(row - 1))
                : "";

        String currentLine = row < lines.size()
                ? "%d: %s%n".formatted(row + 1, lines.get(row))
                : "";

        String nextLine = row + 1 < lines.size()
                ? "%d: %s%n".formatted(row + 2, lines.get(row + 1))
                : "";

        String pointer = " ".repeat(Math.max(column + 2, 0)) + "^ here";

        String message = "%s: %s".formatted(
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );

        return """
Error in template:
(%d:%d) %s
%s%s%s
%s"""
                .formatted(
                        row + 1,
                        column,
                        message,
                        previousLine,
                        currentLine,
                        pointer,
                        nextLine
                );
    }
}
