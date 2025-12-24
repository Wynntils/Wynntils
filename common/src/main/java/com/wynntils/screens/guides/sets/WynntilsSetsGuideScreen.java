/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.sets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
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
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class WynntilsSetsGuideScreen extends WynntilsListScreen<SetInfo, ItemSetGuideButton> {
    private WynntilsSetsGuideScreen() {
        super(Component.translatable("screens.wynntils.wynntilsGuides.sets.name"));
    }

    public static Screen create() {
        return new WynntilsSetsGuideScreen();
    }

    @Override
    protected void doInit() {
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

        renderTitle(poseStack, I18n.get("screens.wynntils.wynntilsGuides.sets.name"));

        renderDescription(poseStack);

        renderVersion(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderPageInfo(poseStack, currentPage + 1, maxPage + 1);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTitle(PoseStack poseStack, String titleString) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CONTENT_BOOK_TITLE, offsetX, 30 + offsetY);

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
                        1.7f);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hovered instanceof ItemSetGuideButton itemSetGuideButton) {
            SetInfo setInfo = itemSetGuideButton.getSetInfo();
            int equippedCount = itemSetGuideButton.getEquippedCount();
            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(Component.empty()
                    .append(Component.literal("Set Bonus").withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" (" + equippedCount + "/"
                                    + setInfo.bonuses().size() + ")")
                            .withStyle(ChatFormatting.GRAY)));

            Map<StatType, Integer> currentBonuses = setInfo.bonuses().get(equippedCount - 1);

            if (currentBonuses.isEmpty()) {
                tooltipLines.add(Component.translatable(
                                "screens.wynntils.wynntilsGuides.sets.setsButton.noBonuses"
                                        + (equippedCount != 1 ? "Plural" : "Singular"),
                                equippedCount)
                        .withStyle(ChatFormatting.GRAY));
            } else {
                List<StatType> sortedStats = Models.Stat.getSortedStats(
                        setInfo.bonuses().get(equippedCount - 1).keySet(), StatListOrdering.WYNNCRAFT);

                sortedStats.forEach(stat -> {
                    int value = setInfo.bonuses().get(equippedCount - 1).get(stat);

                    MutableComponent statComponent = Component.empty();
                    ChatFormatting statColor =
                            value >= 0 || stat.displayAsInverted() ? ChatFormatting.GREEN : ChatFormatting.RED;

                    // No "-" needs to be appended as the value already has it
                    if (value >= 0) {
                        statComponent.append(Component.literal("+").withStyle(statColor));
                    }

                    statComponent.append(
                            Component.literal(String.valueOf(value)).withStyle(statColor));

                    if (stat.getUnit() != StatUnit.RAW) {
                        statComponent.append(Component.literal(stat.getUnit().getDisplayName())
                                .withStyle(statColor));
                    }

                    statComponent.append(Component.literal(" "));

                    statComponent.append(
                            Component.literal(stat.getDisplayName()).withStyle(ChatFormatting.GRAY));
                    tooltipLines.add(statComponent);
                });
            }

            tooltipLines.add(Component.empty());
            tooltipLines.add(Component.literal(setInfo.name() + " Set").withStyle(ChatFormatting.GREEN));
            setInfo.items().forEach(item -> {
                tooltipLines.add(Component.empty()
                        .append(Component.literal("- ").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(item).withStyle(ChatFormatting.DARK_GREEN)));
            });

            tooltipLines.add(Component.empty());
            if (equippedCount < setInfo.bonuses().size()) {
                tooltipLines.add(Component.empty()
                        .append(Component.literal("\uE000")
                                .withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("keybind"))))
                        .append(" ")
                        .append(Component.translatable(
                                "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                        + (equippedCount + 1 != 1 ? "Plural" : "Singular"),
                                equippedCount + 1)));
            }
            if (equippedCount > 1) {
                tooltipLines.add(Component.empty()
                        .append(Component.literal("\uE001")
                                .withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("keybind"))))
                        .append(" ")
                        .append(Component.translatable(
                                "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                        + (equippedCount - 1 != 1 ? "Plural" : "Singular"),
                                equippedCount - 1)));
            }

            guiGraphics.renderComponentTooltip(FontRenderer.getInstance().getFont(), tooltipLines, mouseX, mouseY);
            return;
        }

        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderDescription(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.wynntilsGuides.sets.description")),
                        20 + offsetX,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 10 + offsetX,
                        80 + offsetY,
                        Texture.CONTENT_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        TextShadow.NONE);
    }

    @Override
    protected ItemSetGuideButton getButtonFromElement(int i) {
        int offset = i % getElementsPerPage();
        return new ItemSetGuideButton(
                (int) (Texture.CONTENT_BOOK_BACKGROUND.width() / 2f + 15 + offsetX),
                offset * 13 + 25 + offsetY,
                Texture.CONTENT_BOOK_BACKGROUND.width() / 2 - 37,
                9,
                elements.get(i));
    }

    @Override
    protected void reloadElementsList(String searchTerm) {
        elements.addAll(Models.Set.getSets().stream()
                .filter(setInfo -> StringUtils.partialMatch(setInfo.name(), searchTerm))
                .toList());
    }
}
