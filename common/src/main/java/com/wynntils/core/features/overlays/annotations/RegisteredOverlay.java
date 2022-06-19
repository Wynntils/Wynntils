/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays.annotations;

import com.wynntils.mc.event.RenderEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisteredOverlay {
    /* Whether the annotated overlay instantiated by default */
    boolean enabled() default true;

    RenderState renderAt() default RenderState.Pre;

    RenderEvent.ElementType renderType() default RenderEvent.ElementType.GUI;

    enum RenderState {
        Pre,
        Post
    }
}
