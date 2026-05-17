package com.wynntils.templates.compiler;

import com.wynntils.templates.language.Template;

public class CompilerBackend implements TemplateBackend {

    private static class ExpandedClassLoader extends ClassLoader {

        private ExpandedClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> define(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    private final ExpandedClassLoader classLoader;
    public CompilerBackend(ClassLoader parentClassLoader) {
        classLoader = new ExpandedClassLoader(parentClassLoader);
    }

    @Override
    public String evaluate(Template template) {
        return "";
    }
}
