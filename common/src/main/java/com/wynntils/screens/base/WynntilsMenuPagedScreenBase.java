/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.network.chat.Component;

public abstract class WynntilsMenuPagedScreenBase extends WynntilsMenuScreenBase {
    protected WynntilsMenuPagedScreenBase(Component component) {
        super(component);
    }

    public abstract int getCurrentPage();

    public abstract void setCurrentPage(int currentPage);

    public abstract int getMaxPage();

    protected void renderPageInfo(PoseStack poseStack, int currentPage, int maxPage) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        (currentPage) + " / " + (maxPage),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        TextShadow.NONE);
    }
}
