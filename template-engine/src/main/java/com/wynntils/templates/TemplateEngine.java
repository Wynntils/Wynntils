package com.wynntils.templates;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.templates.compiler.TemplateBackend;
import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.TemplateLanguage;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateEngine {
    private final Map<String, FunctionDefinition> functions = new HashMap<>();
    private final TemplateBackend backend;
    private final TemplateLanguage language;

    public TemplateEngine(TemplateBackend backend) {
        this.backend = backend;
        this.language = new TemplateLanguage(this);
    }

    public void registerFunctions(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            TemplateFunction annotation = method.getAnnotation(TemplateFunction.class);

            if (annotation != null) {
                FunctionDefinition functionDef = new FunctionDefinition(annotation.name(), annotation.aliases(), method, method.getReturnType(), method.getParameterTypes());

                functions.put(annotation.name(), functionDef);
            }
        }
    }

    public FunctionDefinition getFunction(String name) {
        return functions.get(name);
    }

    public List<FunctionDefinition> getFunctions() {
        return functions.values().stream().toList();
    }

    public String evaluate(String input) {
        return backend.evaluate(language.parse(input));
    }
}
