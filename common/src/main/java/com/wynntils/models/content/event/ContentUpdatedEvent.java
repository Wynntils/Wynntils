/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content.event;

import com.wynntils.models.content.type.ContentType;
import net.minecraftforge.eventbus.api.Event;

public class ContentUpdatedEvent extends Event {
    private final ContentType contentType;

    public ContentUpdatedEvent(ContentType contentType) {
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return contentType;
    }
}
