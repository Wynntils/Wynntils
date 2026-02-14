/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays.annotations;

import com.wynntils.utils.type.RenderElementType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterOverlay {
    // This will make all overlays render above everything besides chat and the player tab list by default
    RenderElementType renderType() default RenderElementType.CHAT;
}
