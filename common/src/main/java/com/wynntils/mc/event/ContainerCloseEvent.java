/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;

public abstract class ContainerCloseEvent extends BaseEvent {
    public static class Pre extends ContainerCloseEvent implements OperationCancelable {}

    public static class Post extends ContainerCloseEvent {}
}
