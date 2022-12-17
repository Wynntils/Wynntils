/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.widgets.BackButton;
import com.wynntils.gui.widgets.LootrunButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.LootrunModel;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;

public class WynntilsLootrunsScreen extends WynntilsMenuListScreen<LootrunModel.LootrunInstance, LootrunButton> {
    private WynntilsLootrunsScreen() {
        super(new TranslatableComponent("screens.wynntils.lootruns.name"));
    }

    public static Screen create() {
        return WynntilsScreenWrapper.create(new WynntilsLootrunsScreen());
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        super.onClose();
    }

    @Override
    protected void init() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        super.init();

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

    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hovered instanceof LootrunButton lootrunButton) {
            List<Component> tooltipLines;

            LootrunModel.LootrunInstance currentLootrun = Models.Lootrun.getCurrentLootrun();
            if (currentLootrun != null
                    && Objects.equals(lootrunButton.getLootrun().name(), currentLootrun.name())) {
                tooltipLines = List.of(
                        new TextComponent(lootrunButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.loaded")
                                .withStyle(ChatFormatting.YELLOW),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.unload")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipLines = List.of(
                        new TextComponent(lootrunButton.getLootrun().name()).withStyle(ChatFormatting.BOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.load")
                                .withStyle(ChatFormatting.GREEN),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.viewInFolder")
                                .withStyle(ChatFormatting.GOLD),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.openOnMap")
                                .withStyle(ChatFormatting.BLUE),
                        new TranslatableComponent("screens.wynntils.lootruns.lootrunButton.remove")
                                .withStyle(ChatFormatting.RED));
            }

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    100,
                    tooltipLines,
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.lootruns.name"));

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        if (elements.isEmpty()) {
            renderNoElementsHelper(poseStack, I18n.get("screens.wynntils.lootruns.noLootruns"));
        }

        renderDescription(poseStack);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void renderDescription(PoseStack poseStack) {
        LootrunModel.LootrunInstance currentLootrun = Models.Lootrun.getCurrentLootrun();
        if (currentLootrun != null) {
            poseStack.pushPose();
            poseStack.translate(20, 80, 0);

            poseStack.pushPose();
            poseStack.scale(1.4f, 1.4f, 0f);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            currentLootrun.name(),
                            0,
                            0,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);
            poseStack.popPose();

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.chests") + ": "
                                    + currentLootrun.chests().size(),
                            0,
                            19,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.notes") + ": "
                                    + currentLootrun.notes().size(),
                            0,
                            29,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            Vec3 start = currentLootrun.path().points().get(0);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.start") + ": "
                                    + String.format("[%d, %d, %d]", (int) start.x, (int) start.y, (int) start.z),
                            0,
                            39,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            Vec3 end = currentLootrun
                    .path()
                    .points()
                    .get(currentLootrun.path().points().size() - 1);
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.end") + ": "
                                    + String.format("[%d, %d, %d]", (int) end.x, (int) end.y, (int) end.z),
                            0,
                            49,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            VerticalAlignment.Top,
                            FontRenderer.TextShadow.NONE);

            poseStack.popPose();
        } else {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.description1"),
                            20,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                            80,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            FontRenderer.TextShadow.NONE);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            I18n.get("screens.wynntils.lootruns.description2"),
                            20,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                            155,
                            Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                            CommonColors.BLACK,
                            HorizontalAlignment.Left,
                            FontRenderer.TextShadow.NONE);
        }
    }

    @Override
    protected LootrunButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new LootrunButton(
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 15,
                offset * 13 + 25,
                Texture.QUEST_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i),
                this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Models.Lootrun.getLootruns().stream()
                .filter(lootrunInstance -> StringUtils.partialMatch(lootrunInstance.name(), searchTerm))
                .toList());
    }
}
