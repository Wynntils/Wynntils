/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureInfo {
    /** Subjective stability of feature */
    Stability stability() default Stability.STABLE;

    /**
     * How stable a feature is to wynncraft changes, how able it is to be maintained, and other similar
     * judgements. Subjective.
     */
    enum Stability {
        EXPERIMENTAL,
        UNSTABLE,
        STABLE,
        INVARIABLE
    }

    /** The parent category for options within this container. Also defines file structure. */
    FeatureCategory category() default
            FeatureCategory.UNCATEGORIZED; // If left blank, signifies that this feature has no config options

    /** Whether this container should be visible to the user */
    boolean visible() default true;
}
