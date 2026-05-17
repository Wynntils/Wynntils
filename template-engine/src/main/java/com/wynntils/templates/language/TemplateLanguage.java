/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.TemplateEngine;
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
}
