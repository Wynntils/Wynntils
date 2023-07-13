/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.content.type.ContentInfo;

public class ContentItem extends GuiItem {
    private final ContentInfo contentInfo;

    public ContentItem(ContentInfo contentInfo) {
        this.contentInfo = contentInfo;
    }

    public ContentInfo getContentInfo() {
        return contentInfo;
    }

    @Override
    public String toString() {
        return "ContentItem{" + "contentInfo=" + contentInfo + '}';
    }
}
