/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inform what type of thread the event is allowed to be sent on. Events without an
 * explicit annotation is considered to be allowed on Type == RENDER only.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventThread {
    Type value() default Type.RENDER;

    enum Type {
        RENDER, // The main thread a.k.a the Render thread
        IO, // Any Netty Epoll Client IO thread
        WORKER, // A worker thread, from a Minecraft or Wynntils thread pool
        ANY // Any thread at all
    }
}
