/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;

public class CompilerState {
    private List<String> expressionMethods = new ArrayList<>();
    private List<String> partMethods = new ArrayList<>();
    private ClassWriter classWriter;

    public void start(ClassWriter cw) {
        expressionMethods.clear();
        partMethods.clear();
        classWriter = cw;
    }

    public String getNewExpressionMethodName() {
        String methodName = "expr_" + expressionMethods.size();
        expressionMethods.add(methodName);
        return methodName;
    }

    public String getNewPartMethodName() {
        String methodName = "part_" + partMethods.size();
        partMethods.add(methodName);
        return methodName;
    }

    public int getExpressionMethodsCount() {
        return expressionMethods.size();
    }

    public int getPartMethodsCount() {
        return partMethods.size();
    }

    public List<String> getExpressionMethods() {
        return expressionMethods;
    }

    public void setExpressionMethods(List<String> expressionMethods) {
        this.expressionMethods = expressionMethods;
    }

    public List<String> getPartMethods() {
        return partMethods;
    }

    public void setPartMethods(List<String> partMethods) {
        this.partMethods = partMethods;
    }

    public ClassWriter getClassWriter() {
        return classWriter;
    }

    public void setClassWriter(ClassWriter classWriter) {
        this.classWriter = classWriter;
    }
}
