/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterConfig {
    /** The base part of the translation key to use for this option */
    String key() default "";

    /** The subcategory this option belongs in within its container */
    String subcategory() default "";

    /** Whether this option should be visible to users */
    boolean visible() default true;
}
