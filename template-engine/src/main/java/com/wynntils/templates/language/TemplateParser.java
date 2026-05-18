/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.exception.LanguageException;
import com.wynntils.templates.language.expression.Expression;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.expression.LiteralExpression;
import com.wynntils.templates.language.parts.TemplateExpressionPart;
import com.wynntils.templates.language.parts.TemplateLiteralPart;
import com.wynntils.templates.language.parts.TemplatePart;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class TemplateParser {
    private final TemplateEngine engine;
    private List<TemplateLexer.Token> tokens;
    private int pos = 0;

    public TemplateParser(TemplateEngine engine) {
        this.engine = engine;
    }

    public Template parse(List<TemplateLexer.Token> tokens) {
        this.pos = 0;
        this.tokens = tokens;

        List<TemplatePart> parts = new ArrayList<>();

        while (hasNext() && peek().type() != TemplateLexer.TokenType.EOF) {
            parts.add(parsePart());
        }

        expect(TemplateLexer.TokenType.EOF);

        return new Template(parts);
    }

    private TemplatePart parsePart() {
        TemplateLexer.Token t = peek();

        if (t.type() == TemplateLexer.TokenType.TEXT) {
            return parseTextPart();
        } else if (t.type() == TemplateLexer.TokenType.TEMPLATE_START) {
            return parseExpressionPart();
        }

        throw new LanguageException("Unexpected token: " + t.type());
    }

    private TemplateLiteralPart parseTextPart() {
        TemplateLexer.Token t = expect(TemplateLexer.TokenType.TEXT);
        return new TemplateLiteralPart(t.value());
    }

    private TemplateExpressionPart parseExpressionPart() {
        expect(TemplateLexer.TokenType.TEMPLATE_START); // {
        Expression expr = parseExpression();

        expect(TemplateLexer.TokenType.TEMPLATE_END); // }

        return new TemplateExpressionPart(expr);
    }

    private Expression parseExpression() {
        TemplateLexer.Token t = peek();

        if (t.type() == TemplateLexer.TokenType.IDENTIFIER) {
            return parseFunctionExpression();
        } else if (t.type() == TemplateLexer.TokenType.STRING) {
            return new LiteralExpression(next().value(), String.class);
        } else if (t.type() == TemplateLexer.TokenType.NUMBER) {
            return new LiteralExpression(Double.parseDouble(next().value()), double.class);
        } else {
            throw new LanguageException("Unexpected token in expression: " + t.type());
        }
    }

    private FunctionExpression parseFunctionExpression() {
        String identifier = expect(TemplateLexer.TokenType.IDENTIFIER).value();

        if (peek().type() == TemplateLexer.TokenType.ARGUMENTS_START) {
            Expression[] args = parseArguments();

            FunctionDefinition def = engine.getFunction(identifier, args.length);
            if (def == null) def = engine.getFunction(identifier, 1);

            return new FunctionExpression(identifier, args, def);
        } else  {
            return new FunctionExpression(identifier, engine.getFunction(identifier, 0));
        }
    }

    private Expression[] parseArguments() {
        List<Expression> args = new ArrayList<>();

        expect(TemplateLexer.TokenType.ARGUMENTS_START); // (

        while (peek().type() != TemplateLexer.TokenType.ARGUMENTS_END) {
            args.add(parseExpression());

            if (peek().type() == TemplateLexer.TokenType.SEMICOLON) {
                next(); // ;
            } else if (peek().type() != TemplateLexer.TokenType.ARGUMENTS_END) {
                throw new LanguageException("Expected ; or ) but got " + peek().type());
            }
        }

        expect(TemplateLexer.TokenType.ARGUMENTS_END); // )

        return args.toArray(new Expression[0]);
    }

    private boolean hasNext() {
        return pos < tokens.size();
    }

    private TemplateLexer.Token peek() {
        return hasNext() ? tokens.get(pos) : null;
    }

    private TemplateLexer.Token next() {
        return hasNext() ? tokens.get(pos++) : null;
    }

    private TemplateLexer.Token expect(TemplateLexer.TokenType type) {
        TemplateLexer.Token t = next();
        if (t == null || t.type() != type) {
            throw new LanguageException("Expected " + type + " but got " + (t == null ? "EOF" : t.type()));
        }
        return t;
    }
}
