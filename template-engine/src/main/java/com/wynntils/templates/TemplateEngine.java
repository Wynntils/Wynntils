/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.templates.backends.TemplateBackend;
import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.TemplateLanguage;
import com.wynntils.templates.language.exception.LanguageException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateEngine {
    public record FunctionKey(String name, int argCount) {}

    private final Map<FunctionKey, FunctionDefinition> functions = new HashMap<>();
    private final TemplateBackend backend;
    private final TemplateLanguage language;
    private String error = "";

    public TemplateEngine(TemplateBackend backend) {
        this.backend = backend;
        this.language = new TemplateLanguage(this);
    }

    public void registerFunctions(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            TemplateFunction annotation = method.getAnnotation(TemplateFunction.class);

            if (annotation != null) {
                FunctionDefinition functionDef = new FunctionDefinition(
                        annotation.name(),
                        annotation.aliases(),
                        method,
                        method.getReturnType(),
                        method.getParameterTypes(),
                        annotation.isPure());

                functions.put(new FunctionKey(functionDef.name(), method.getParameterCount()), functionDef);

                for (String alias : annotation.aliases()) {
                    functions.put(new FunctionKey(alias, method.getParameterCount()), functionDef);
                }
            }
        }
    }

    public FunctionDefinition getFunction(String name, int argCount) {
        return functions.get(new FunctionKey(name, argCount));
    }

    public boolean hasFunction(String name, int argCount) {
        return functions.containsKey(new FunctionKey(name, argCount));
    }

    public List<FunctionDefinition> getFunctions() {
        return functions.values().stream().toList();
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return !error.isEmpty();
    }

    public String evaluate(String input) {
        error = "";
        try {
            return backend.evaluate(language.parse(input));
        } catch (LanguageException lexException) {
            error = language.formatError(input, lexException);
            return "Error evaluating template";
        } catch (RuntimeException e) {
            error =
                    "Template crashed during evaluation, report this to the Wynntils developers with the template and stack trace:\n"
                            + e.getMessage();
            return "Unexpected error evaluating template";
        }
    }
}
