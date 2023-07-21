/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.content;

import com.wynntils.core.components.Models;
import com.wynntils.models.content.CaveInfo;
import com.wynntils.models.content.type.ContentSortOrder;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.SortableContentScreen;
import com.wynntils.screens.content.widgets.CaveButton;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.network.chat.Component;

public final class WynntilsCaveScreen extends WynntilsListScreen<CaveInfo, CaveButton>
        implements SortableContentScreen {
    private ContentSortOrder contentSortOrder = ContentSortOrder.LEVEL;
    private CaveInfo trackingRequested = null;

    private WynntilsCaveScreen() {
        super(Component.translatable("screens.wynntils.wynntilsCaveBook.name"));
    }

    public static WynntilsCaveScreen create() {
        return new WynntilsCaveScreen();
    }

    @Override
    protected CaveButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new CaveButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Models.Cave.getSortedCaves(contentSortOrder).stream()
                .filter(info -> StringUtils.partialMatch(info.getName(), searchTerm))
                .toList());
    }

    @Override
    public ContentSortOrder getContentSortOrder() {
        return contentSortOrder;
    }

    @Override
    public void setContentSortOrder(ContentSortOrder newSortOrder) {
        if (newSortOrder == null) {
            throw new IllegalStateException("Tried to set null content sort order");
        }

        this.contentSortOrder = newSortOrder;
        this.setCurrentPage(0);
    }

    public void setTrackingRequested(CaveInfo caveInfo) {
        this.trackingRequested = caveInfo;
    }

    public CaveInfo getTrackingRequested() {
        return trackingRequested;
    }
}
