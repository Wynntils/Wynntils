/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.testutils;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class TestPrivateFieldsUtil {
    public static Field getPrivateField(Class targetClass, String name) {
        try {
            Field declaredField = targetClass.getDeclaredField(name);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Pattern getPrivateStaticRegexPattern(Class targetClass, String name) {
        Field field = getPrivateField(targetClass, name);
        try {
            return (Pattern) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
