/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
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
public @interface OverlayGroup {
    // How many instances of this overlay should exist by default
    int instances() default 1;

    RenderEvent.ElementType renderType();

    RenderState renderAt() default RenderState.POST;
}
