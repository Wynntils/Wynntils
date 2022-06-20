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
public @interface Overlay {
    /* Whether the annotated overlay instantiated at registration */
    boolean enabled() default true;

    RenderEvent.ElementType renderType();

    RenderState renderAt() default RenderState.Pre;

    enum RenderState {
        Pre,
        Post
    }
}
