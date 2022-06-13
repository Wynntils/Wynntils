/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.overlays.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Overlay {
    /** The display name for this overlay, for users*/
    String displayName();

    /** The description for the toggle of this overlay, for users */
    String description() default "";

    /** The subcategory the toggle will belong to within the category */
    String subcategory() default "";

    /** Whether this option should be visible to users */
    boolean visible() default true;
}
