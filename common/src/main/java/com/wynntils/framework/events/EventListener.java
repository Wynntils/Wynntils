/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventListener {
    private final Method method;
    private final Class<?> parent;

    public EventListener(Method method, Class<?> parent) {
        // if (!Modifier.isStatic(method.getModifiers())) throw new
        // IllegalArgumentException(String.format("%s#%s is not static",
        // method.getDeclaringClass().getSimpleName(), method.getName()));
        // if (method.getParameterCount() != 1) throw new
        // IllegalArgumentException(String.format("%s#%s does not have 1 parameter",
        // method.getDeclaringClass().getSimpleName(), method.getName()));

        this.method = method;
        method.setAccessible(true);
        // if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) throw new
        // IllegalArgumentException(String.format("%s#%s does not have 1 parameter",
        // method.getDeclaringClass().getSimpleName(), method.getName()));
        this.parent = parent;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getParent() {
        return parent;
    }

    public void accept(Object value) throws InvocationTargetException, IllegalAccessException {
        method.invoke(null, value);
    }
}
