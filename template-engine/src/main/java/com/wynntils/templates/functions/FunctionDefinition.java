/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.functions;

import java.lang.reflect.Method;

public record FunctionDefinition(
        String name, String[] aliases, Method method, Class<?> returnType, Class<?>[] parameterTypes, boolean isPure) {
    public boolean isVarArgs() {
        return parameterTypes().length == 1 && parameterTypes()[0].isArray();
    }

    public Class<?> getVarArgType() {
        if (!isVarArgs()) return Void.class;
        return parameterTypes()[0].getComponentType();
    }
}
