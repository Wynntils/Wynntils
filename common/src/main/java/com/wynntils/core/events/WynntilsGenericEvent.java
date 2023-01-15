/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import java.lang.reflect.Type;
import net.minecraftforge.eventbus.api.IGenericEvent;

public class WynntilsGenericEvent<T> extends WynntilsEvent implements IGenericEvent<T> {
    private Class<T> type;

    protected WynntilsGenericEvent(Class<T> type) {
        this.type = type;
    }

    @Override
    public Type getGenericType() {
        return type;
    }
}
