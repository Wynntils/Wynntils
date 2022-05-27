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

    /** Subjective Gameplay impact of feature */
    GameplayImpact gameplay() default GameplayImpact.MEDIUM;

    /** Subjective Performance impact of feature */
    PerformanceImpact performance() default PerformanceImpact.MEDIUM;

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

    /**
     * How much impact on gameplay a feature has. For example, a more important feature like a minimap
     * would have a larger impact than a smaller feature like blocking health pots. Subjective.
     */
    enum GameplayImpact {
        SMALL,
        MEDIUM,
        LARGE,
        EXTREME
    }

    /**
     * How much performance strain the feature causes. For example, cached chunks would have a higher
     * performance impact than a Wynncraft Button. Subjective.
     */
    enum PerformanceImpact {
        SMALL,
        MEDIUM,
        LARGE,
        EXTREME
    }
}
