package com.wynntils.templates.compiler;

import com.wynntils.templates.language.Template;

public interface TemplateBackend {
    String evaluate(Template template);
}
