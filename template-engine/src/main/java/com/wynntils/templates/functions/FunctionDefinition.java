package com.wynntils.templates.functions;

import java.lang.reflect.Method;

public record FunctionDefinition(String name, String[] aliases, Method method, Class<?> returnType, Class<?>[] parameterTypes) {
}
