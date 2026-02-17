/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.misc;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsMiscGuideScreen extends WynntilsListScreen<GuideItemStack, WynntilsButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private final List<GuideItemStack> parsedItemCache = new ArrayList<>();

    private WynntilsMiscGuideScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.misc.name"));
    }

    public static Screen create() {
        return new WynntilsMiscGuideScreen();
    }

    @Override
    protected void doInit() {
        if (parsedItemCache.isEmpty()) {
            for (RuneType runeType : Models.Rewards.getAllRuneInfo()) {
                parsedItemCache.add(new RuneItemStack(runeType));
            }

            for(Dungeon dungeon : Dungeon.values()) {
                if(dungeon.isExists()) parsedItemCache.add(new GuideDungeonKeyItemStack(dungeon, false));
                if(dungeon.isCorruptedExists()) parsedItemCache.add(new GuideDungeonKeyItemStack(dungeon, true));
            }
        }

        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f) + offsetX,
                65 + offsetY,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsGuidesListScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f
                        + 50
                        - Texture.FORWARD_ARROW_OFFSET.width() / 2f
                        + offsetX),
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50 + offsetX,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25 + offsetY,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);

        renderTitle(guiGraphics, I18n.get("screens.wynntils.wynntilsGuides.misc.name"));

        renderDescription(guiGraphics, I18n.get("screens.wynntils.wynntilsGuides.guideDescription"), "");

        renderVersion(guiGraphics);

        renderItemsHeader(guiGraphics);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(guiGraphics, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof GuideRuneItemStackButton guideAugmentItemStack) {
            guiGraphics.setTooltipForNextFrame(
                    FontRenderer.getInstance().getFont(), guideAugmentItemStack.getItemStack(), mouseX, mouseY);
        } else if (hovered instanceof GuideDungeonKeyItemStackButton guideDungeonKeyItemStack) {
            guiGraphics.setTooltipForNextFrame(
                    FontRenderer.getInstance().getFont(), guideDungeonKeyItemStack.getItemStack(), mouseX, mouseY);
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemsHeader(GuiGraphics guiGraphics) {
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available")),
                        Texture.CONTENT_BOOK_BACKGROUND.width() * 0.75f + offsetX,
                        30 + offsetY,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    @Override
    protected WynntilsButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        GuideItemStack element = elements.get(i);

        if(element instanceof GuideDungeonKeyItemStack guideDungeonKeyItemStack) {
            return new GuideDungeonKeyItemStackButton(
                    (int) (xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 13 + offsetX),
                    yOffset + 43 + offsetY,
                    18,
                    18,
                    guideDungeonKeyItemStack,
                    this);
        } else if (element instanceof RuneItemStack runeItemStack) {
            return new GuideRuneItemStackButton(
                    (int) (xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 13 + offsetX),
                    yOffset + 43 + offsetY,
                    18,
                    18,
                    runeItemStack,
                    this);
        }

        return null;
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(parsedItemCache.stream()
                .filter(itemStack -> StringUtils.partialMatch(
                        StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting(), searchTerm))
                .toList());
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
