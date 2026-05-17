package com.wynntils.templates.language;

import com.wynntils.templates.language.parts.TemplatePart;

import java.util.List;

public class Template {
    private final List<TemplatePart> parts;
    public Template(List<TemplatePart> parts) {
        this.parts = parts;
    }
    public List<TemplatePart> getParts() {
        return parts;
    }
}
