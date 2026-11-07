/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.sets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.handlers.tooltip.components.PaginatorLineBuilder;
import com.wynntils.handlers.tooltip.components.SetBonusLineBuilder;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class SetGuideButton extends GuideButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(116, 108, 132);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(96, 89, 110);

    private final SetInfo setInfo;
    private static final Map<String, SetButtonState> SET_BUTTON_STATES = new HashMap<>();

    private final GearTier setTier;

    private int equippedCount = 1;
    private boolean statsPage = true;

    private List<Component> generatedTooltip = null;

    public SetGuideButton(int x, int y, SetInfo setInfo) {
        super(x, y, 80, null);
        this.setInfo = setInfo;

        setTier = Models.Set.getSetGearTier(setInfo);

        SetButtonState state = SET_BUTTON_STATES.get(setInfo.name());
        if (state != null) {
            equippedCount = state.equippedCount();
            statsPage = state.statsPage();
        }
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics,
                isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR,
                this.getX(),
                this.getY(),
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderScrollingAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(BannerBoxFont.buildMessage(
                                setInfo.cleanName(), setTier.getSecondaryColor(), CommonColors.BLACK, "")),
                        this.getX() + 2,
                        this.getX() + this.getWidth() - 4,
                        this.getY() + 1,
                        this.getY() + this.getHeight() - 2,
                        this.width - 4,
                        CommonColors.BLACK,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (statsPage) {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                equippedCount = Math.min(equippedCount + 1, setInfo.bonuses().size());
                saveState();
                generatedTooltip = null;
                return true;
            } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                equippedCount = Math.max(1, equippedCount - 1);
                saveState();
                generatedTooltip = null;
                return true;
            }
        }

        if (event.button() == InputConstants.MOUSE_BUTTON_MIDDLE) {
            statsPage = !statsPage;
            saveState();
            generatedTooltip = null;
            return true;
        }

        return false;
    }

    private void saveState() {
        SET_BUTTON_STATES.put(setInfo.name(), new SetButtonState(equippedCount, statsPage));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (generatedTooltip == null) {
            generatedTooltip = buildTooltip();
        }

        guiGraphics.setTooltipForNextFrame(
                FontRenderer.getInstance().getFont(),
                Lists.transform(generatedTooltip, Component::getVisualOrderText),
                mouseX,
                mouseY,
                Identifier.withDefaultNamespace(setTier.getApiName()));
    }

    private List<Component> buildTooltip() {
        List<TooltipElement> tooltipLines = new ArrayList<>();

        if (statsPage) {
            tooltipLines.add(SetBonusLineBuilder.buildHeader(
                    setInfo.cleanName(),
                    setTier,
                    equippedCount,
                    setInfo.bonuses().size()));

            Map<StatType, Integer> currentBonuses =
                    setInfo.bonuses().get(equippedCount).minor();

            if (currentBonuses.isEmpty()) {
                Component noBonusesLine = Component.translatable(
                                "screens.wynntils.wynntilsGuides.sets.setsButton.noBonuses"
                                        + (equippedCount != 1 ? "Plural" : "Singular"),
                                equippedCount)
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY));
                tooltipLines.add(TooltipLine.center(noBonusesLine));
            } else {
                tooltipLines.addAll(SetBonusLineBuilder.buildBonuses(currentBonuses, null, RangedValue.NONE));
            }

            tooltipLines.add(TooltipLine.empty());
            if (equippedCount < setInfo.bonuses().size()) {
                Component incrementLine = Component.empty()
                        .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class)
                                .withoutShadow())
                        .append(" ")
                        .append(Component.translatable(
                                        "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                                + (equippedCount + 1 != 1 ? "Plural" : "Singular"),
                                        equippedCount + 1)
                                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));
                tooltipLines.add(TooltipLine.left(incrementLine));
            }
            if (equippedCount > 1) {
                Component decrementLine = Component.empty()
                        .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class)
                                .withoutShadow())
                        .append(" ")
                        .append(Component.translatable(
                                        "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                                + (equippedCount != 2 ? "Plural" : "Singular"),
                                        equippedCount - 1)
                                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));
                tooltipLines.add(TooltipLine.left(decrementLine));
            }
        } else {
            tooltipLines.add(TooltipLine.left(SetBonusLineBuilder.buildSetName(setInfo.cleanName(), setTier)));
            if (!setInfo.items().isEmpty()) {
                setInfo.items().forEach(item -> {
                    tooltipLines.add(TooltipLine.left(Component.empty()
                            .append(Component.literal(item).withStyle(ChatFormatting.WHITE))
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))));
                });
            } else {
                Component noItemsLine = Component.translatable(
                                "screens.wynntils.wynntilsGuides.sets.setsButton.noItems")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE));
                tooltipLines.add(TooltipLine.center(noItemsLine));
            }
        }

        Component mouseIcon =
                Component.literal("\uE003").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS));
        tooltipLines.add(PaginatorLineBuilder.buildPaginator(mouseIcon, statsPage ? 0 : 1, 2));
        tooltipLines.add(TooltipLine.empty());

        return Handlers.Tooltip.alignTooltip(tooltipLines);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.NONE;
    }

    private record SetButtonState(int equippedCount, boolean statsPage) {}
}
