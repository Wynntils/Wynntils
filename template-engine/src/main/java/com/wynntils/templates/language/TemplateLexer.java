/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.language.exception.LanguageException;
import java.util.ArrayList;
import java.util.List;

class TemplateLexer {
    public enum TokenType {
        TEMPLATE_START,
        TEMPLATE_END,
        IDENTIFIER,
        STRING,
        NUMBER,
        ARGUMENTS_START,
        ARGUMENTS_END,
        HASH,
        COMMENT,
        SEMICOLON,
        TEXT,
        EOF
    }

    private enum Mode {
        TEXT,
        TEMPLATE,
        STRING,
        DIRECTIVE
    }

    public record Token(TokenType type, String value) {}

    private final List<Token> tokens = new ArrayList<>();

    private int pos = 0;
    private String input;

    private Mode mode = Mode.TEXT;

    public List<Token> tokenize(String input) {
        tokens.clear();
        this.input = input;
        pos = 0;

        while (!isAtEnd()) {
            switch (mode) {
                case TEXT -> lexText();
                case TEMPLATE -> lexTemplate();
                case DIRECTIVE -> lexDirective();
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private void lexDirective() {
        StringBuilder text = new StringBuilder();

        while (!isAtEnd()) {
            if (peek() == '\n') {
                break;
            }

            text.append(advance());
        }

        if (!text.isEmpty()) {
            tokens.add(new Token(TokenType.COMMENT, text.toString()));
        }

        mode = Mode.TEXT;
    }

    private void lexText() {
        StringBuilder text = new StringBuilder();

        while (!isAtEnd()) {
            if (peek() == '{') {
                break;
            }

            if ((peek() == '\n' || pos == 0) && peekNext() == '#') {
                break;
            }

            text.append(advance());
        }

        if (!text.isEmpty()) {
            tokens.add(new Token(TokenType.TEXT, text.toString()));
        }

        if (match('{')) {
            tokens.add(new Token(TokenType.TEMPLATE_START, "{"));
            mode = Mode.TEMPLATE;
        }

        if ((match('\n') || pos == 0) && match('#')) {
            tokens.add(new Token(TokenType.HASH, "#"));
            mode = Mode.DIRECTIVE;
        }
    }

    private void lexTemplate() {
        skipWhitespace();

        if (peek() == '}') {
            advance();

            tokens.add(new Token(TokenType.TEMPLATE_END, "}"));
            mode = Mode.TEXT;
            return;
        }

        char c = peek();

        if (isIdentifierStart(c)) {
            lexIdentifier();
            return;
        }

        if (Character.isDigit(c)) {
            lexNumber();
            return;
        }

        switch (advance()) {
            case '(' -> tokens.add(new Token(TokenType.ARGUMENTS_START, "("));
            case ')' -> tokens.add(new Token(TokenType.ARGUMENTS_END, ")"));
            case ';' -> tokens.add(new Token(TokenType.SEMICOLON, ";"));
            case '"' -> lexString();
            case ':' -> throw new LanguageException("Unexpected character: ':' (using colons as formatters is no longer supported)");
            default -> throw new LanguageException("Unexpected character: " + c);
        }
    }

    private void skipWhitespace() {
        while (match(' ')) {}
    }

    private void lexNumber() {
        StringBuilder sb = new StringBuilder();

        while (Character.isDigit(peek())) {
            sb.append(advance());
        }

        if (peek() == '.') {
            do {
                sb.append(advance());
            } while (Character.isDigit(peek()));
        }

        tokens.add(new Token(TokenType.NUMBER, sb.toString()));
    }

    private void lexIdentifier() {
        StringBuilder sb = new StringBuilder();

        while (isIdentifierPart(peek())) {
            sb.append(advance());
        }

        tokens.add(new Token(TokenType.IDENTIFIER, sb.toString()));
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private void lexString() {
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\\') {
                advance();

                char escaped = advance();

                switch (escaped) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> throw new LanguageException("Invalid escape: \\" + escaped);
                }
            } else {
                sb.append(advance());
            }
        }

        if (isAtEnd()) {
            throw new LanguageException("Unterminated string");
        }

        advance(); // closing "

        tokens.add(new Token(TokenType.STRING, sb.toString()));
    }

    private boolean isAtEnd() {
        return pos >= input.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : input.charAt(pos);
    }

    private char peekNext() {
        return pos + 1 >= input.length() ? '\0' : input.charAt(pos + 1);
    }

    private char advance() {
        return input.charAt(pos++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(pos) != expected) return false;

        pos++;
        return true;
    }
}
