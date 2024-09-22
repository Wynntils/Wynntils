/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.crowdsourcing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.crowdsourcing.widgets.CrowdSourcedDataWidget;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.render.Texture;
import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class WynntilsCrowdSourcingSettingsScreen
        extends WynntilsListScreen<CrowdSourcedDataType, CrowdSourcedDataWidget> {
    protected WynntilsCrowdSourcingSettingsScreen() {
        super(Component.translatable("screens.wynntils.wynntilsCrowdSourcing.name"));
    }

    public static WynntilsCrowdSourcingSettingsScreen create() {
        return new WynntilsCrowdSourcingSettingsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW_OFFSET.width() / 2,
                Texture.BACK_ARROW_OFFSET.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.CONTENT_BOOK_BACKGROUND.width() - 50,
                Texture.CONTENT_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW_OFFSET.width() / 2,
                Texture.FORWARD_ARROW_OFFSET.height(),
                true,
                this));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsCrowdSourcing.name"));

        renderVersion(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderDescription(poseStack, I18n.get("screens.wynntils.wynntilsCrowdSourcing.screenDescription"), "");

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CrowdSourcedDataWidget getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new CrowdSourcedDataWidget(
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Arrays.stream(CrowdSourcedDataType.values())
                .filter(dataType -> StringUtils.partialMatch(dataType.getTranslatedName(), searchTerm))
                .toList());
    }
}
