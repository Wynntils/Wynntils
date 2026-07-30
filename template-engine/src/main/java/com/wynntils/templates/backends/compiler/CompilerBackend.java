/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import com.wynntils.templates.backends.TemplateBackend;
import com.wynntils.templates.backends.compiler.exceptions.TemplateCompileException;
import com.wynntils.templates.backends.compiler.exceptions.TemplateExecutionException;
import com.wynntils.templates.language.Template;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CompilerBackend implements TemplateBackend {
    private static class ExpandedClassLoader extends ClassLoader {
        private ExpandedClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> define(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    private final ClassLoader parentClassLoader;
    private final Map<Template, Supplier<String>> compiledTemplates = new HashMap<>();
    private final FunctionCompiler functionCompiler = new FunctionCompiler();

    public CompilerBackend(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public Supplier<String> compile(Template template) {
        try {
            byte[] bytes = functionCompiler.compile(template);

            System.out.println("Compiling");

            try (FileOutputStream fos = new FileOutputStream("E:/templates/" + template.hashCode() + ".class")) {
                fos.write(bytes);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Class<?> clazz = new ExpandedClassLoader(parentClassLoader).define(bytes);

            Method method = clazz.getMethod("run");

            return () -> invokeCompiledMethod(method);
        } catch (ReflectiveOperationException e) {
            throw new TemplateCompileException("Failed to compile template", e);
        }
    }

    private String invokeCompiledMethod(Method method) {
        try {
            return (String) method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new TemplateExecutionException("Failed to execute compiled template", e);
        }
    }

    @Override
    public String evaluate(Template template) {
        return compiledTemplates.computeIfAbsent(template, this::compile).get();
    }
}
