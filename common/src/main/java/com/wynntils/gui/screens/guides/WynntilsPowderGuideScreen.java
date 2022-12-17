/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.guides;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.WynntilsGuidesListScreen;
import com.wynntils.gui.screens.WynntilsMenuListScreen;
import com.wynntils.gui.screens.WynntilsScreenWrapper;
import com.wynntils.gui.screens.guides.widgets.GuidePowderItemStack;
import com.wynntils.gui.widgets.BackButton;
import com.wynntils.gui.widgets.PageSelectorButton;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.item.PowderItemStack;
import com.wynntils.wynn.item.generator.PowderGenerator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.TooltipFlag;

public class WynntilsPowderGuideScreen extends WynntilsMenuListScreen<PowderItemStack, GuidePowderItemStack> {
    private static final int ELEMENTS_COLUMNS = 7;
    private static final int ELEMENT_ROWS = 7;

    private List<PowderItemStack> parsedItemCache;

    private WynntilsPowderGuideScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsGuides.powder.name"));
    }

    public static Screen create() {
        return WynntilsScreenWrapper.create(new WynntilsPowderGuideScreen());
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        super.onClose();
    }

    @Override
    protected void init() {
        if (parsedItemCache == null) {
            parsedItemCache = PowderGenerator.getAllPowderProfiles().stream()
                    .map(PowderItemStack::new)
                    .toList();
        }

        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        super.init();

        this.addRenderableWidget(new BackButton(
                (int) ((Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 16) / 2f),
                65,
                Texture.BACK_ARROW.width() / 2,
                Texture.BACK_ARROW.height(),
                WynntilsGuidesListScreen.create()));

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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = getTranslationX();
        final float translationY = getTranslationY();
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.powder.name"));

        renderVersion(poseStack);

        renderItemsHeader(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        poseStack.popPose();

        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (hovered instanceof GuidePowderItemStack guidePowderItemStack) {
            PowderItemStack itemStack = guidePowderItemStack.getItemStack();

            List<Component> tooltipLines = itemStack.getTooltipLines(McUtils.player(), TooltipFlag.Default.NORMAL);
            tooltipLines.add(TextComponent.EMPTY);
            if (ItemFavoriteFeature.INSTANCE.favoriteItems.contains(
                    ComponentUtils.getUnformatted(itemStack.getHoverName()))) {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsGuides.itemGuide.unfavorite")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltipLines.add(new TranslatableComponent("screens.wynntils.wynntilsGuides.itemGuide.favorite")
                        .withStyle(ChatFormatting.GREEN));
            }

            RenderUtils.drawTooltipAt(
                    poseStack,
                    mouseX,
                    mouseY,
                    0,
                    ComponentUtils.wrapTooltips(tooltipLines, 200),
                    FontRenderer.getInstance().getFont(),
                    true);
        }
    }

    private void renderItemsHeader(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsGuides.itemGuide.available"),
                        Texture.QUEST_BOOK_BACKGROUND.width() * 0.75f,
                        30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    protected GuidePowderItemStack getButtonFromElement(int i) {
        int xOffset = (i % ELEMENTS_COLUMNS) * 20;
        int yOffset = ((i % getElementsPerPage()) / ELEMENTS_COLUMNS) * 20;

        return new GuidePowderItemStack(
                xOffset + Texture.QUEST_BOOK_BACKGROUND.width() / 2 + 13, yOffset + 43, 18, 18, elements.get(i), this);
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(parsedItemCache.stream()
                .filter(itemStack ->
                        StringUtils.partialMatch(ComponentUtils.getUnformatted(itemStack.getHoverName()), searchTerm))
                .toList());
    }

    @Override
    protected int getElementsPerPage() {
        return ELEMENT_ROWS * ELEMENTS_COLUMNS;
    }
}
