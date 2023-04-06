/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.CodedString;
import com.wynntils.screens.base.WynntilsListScreen;
import com.wynntils.screens.base.widgets.BackButton;
import com.wynntils.screens.base.widgets.PageSelectorButton;
import com.wynntils.screens.guides.emeraldpouch.WynntilsEmeraldPouchGuideScreen;
import com.wynntils.screens.guides.gear.WynntilsItemGuideScreen;
import com.wynntils.screens.guides.ingredient.WynntilsIngredientGuideScreen;
import com.wynntils.screens.guides.powder.WynntilsPowderGuideScreen;
import com.wynntils.screens.guides.widgets.GuidesButton;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WynntilsGuidesListScreen extends WynntilsListScreen<Screen, GuidesButton> {
    private static final List<Screen> GUIDES = List.of(
            WynntilsItemGuideScreen.create(),
            WynntilsIngredientGuideScreen.create(),
            WynntilsEmeraldPouchGuideScreen.create(),
            WynntilsPowderGuideScreen.create());

    private WynntilsGuidesListScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.name"));
    }

    public static Screen create() {
        return new WynntilsGuidesListScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsMenuScreen.create()));

        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 50 - Texture.FORWARD_ARROW.width() / 2,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                false,
                this));
        this.addRenderableWidget(new PageSelectorButton(
                Texture.QUEST_BOOK_BACKGROUND.width() - 50,
                Texture.QUEST_BOOK_BACKGROUND.height() - 25,
                Texture.FORWARD_ARROW.width() / 2,
                Texture.FORWARD_ARROW.height(),
                true,
                this));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.name"));

        renderVersion(poseStack);

        renderWidgets(poseStack, mouseX, mouseY, partialTick);

        renderDescription(poseStack, I18n.get("screens.wynntils.wynntilsGuides.screenDescription"));

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();
    }

    @Override
    protected void renderDescription(PoseStack poseStack, String description) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        CodedString.fromString(description),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        80,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }

    @Override
    protected GuidesButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new GuidesButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i));
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(GUIDES.stream()
                .filter(screen ->
                        StringUtils.partialMatch(ComponentUtils.getUnformatted(screen.getTitle()), searchTerm))
                .toList());
    }
}
