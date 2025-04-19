/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emeraldpouch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.guides.WynntilsGuidesListScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
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

public final class WynntilsEmeraldPouchGuideScreen
        extends WynntilsListScreen<GuideEmeraldPouchItemStack, GuideEmeraldPouchItemStackButton> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<GuideEmeraldPouchItemStack> parsedItemCache;

    private WynntilsEmeraldPouchGuideScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.emeraldPouch.name"));
    }

    public static Screen create() {
        return new WynntilsEmeraldPouchGuideScreen();
    }

    @Override
    protected void doInit() {
        if (parsedItemCache == null) {
            parsedItemCache = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                parsedItemCache.add(new GuideEmeraldPouchItemStack(i));
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
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.emeraldPouch.name"));

        renderDescription(poseStack, I18n.get("screens.wynntils.wynntilsGuides.guideDescription"), "");

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTitle(PoseStack poseStack, String titleString) {
        int txWidth = Texture.CONTENT_BOOK_TITLE.width();
        int txHeight = Texture.CONTENT_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.CONTENT_BOOK_TITLE.resource(),
                offsetX,
                30 + offsetY,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(titleString),
                        10 + offsetX,
                        36 + offsetY,
                        CommonColors.YELLOW,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL,
                        1.5f);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof GuideEmeraldPouchItemStackButton guideEmeraldPouchItemStack) {
            guiGraphics.renderTooltip(
                    FontRenderer.getInstance().getFont(), guideEmeraldPouchItemStack.getItemStack(), mouseX, mouseY);
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available")),
                        Texture.CONTENT_BOOK_BACKGROUND.width() * 0.75f + offsetX,
                        30 + offsetY,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    @Override
    protected GuideEmeraldPouchItemStackButton getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuideEmeraldPouchItemStackButton(
                (int) (xOffset + Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 13 + offsetX),
                yOffset + 43 + offsetY,
                18,
                18,
                elements.get(i),
                this);
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
