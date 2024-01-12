/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays.annotations;

import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.mc.event.RenderEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OverlayInfo {
    /* Whether the annotated overlay enabled by default */
    boolean enabled() default true;

    RenderEvent.ElementType renderType();

    RenderState renderAt() default RenderState.POST;
}
