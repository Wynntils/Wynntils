/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.sets;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.stats.type.StatUnit;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SetGuideButton extends GuideButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(116, 108, 132);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(96, 89, 110);

    private static final CustomColor CURRENT_PAGE = new CustomColor(255, 234, 128);
    private static final CustomColor INACTIVE_PAGE = new CustomColor(69, 84, 73);

    private static final CustomColor POSITIVE_STAT = new CustomColor(172, 250, 198);
    private static final CustomColor NEGATIVE_STAT = new CustomColor(250, 172, 172);

    private final SetInfo setInfo;
    private static final Map<String, SetButtonState> SET_BUTTON_STATES = new HashMap<>();

    private final GearTier setTier;

    private int equippedCount = 1;
    private boolean statsPage = true;

    private List<Component> cachedTooltip = null;

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
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                equippedCount = Math.min(equippedCount + 1, setInfo.bonuses().size());
                saveState();
                cachedTooltip = null;
                return true;
            } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                equippedCount = Math.max(1, equippedCount - 1);
                saveState();
                cachedTooltip = null;
                return true;
            }
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            statsPage = !statsPage;
            saveState();
            cachedTooltip = null;
            return true;
        }

        return false;
    }

    private void saveState() {
        SET_BUTTON_STATES.put(setInfo.name(), new SetButtonState(equippedCount, statsPage));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (cachedTooltip == null) {
            List<Pair<Component, Component>> linePairs = new ArrayList<>();
            MutableComponent setTitle = Component.empty()
                    .append(BannerBoxFont.buildMessage(
                            "set", CustomColor.fromChatFormatting(setTier.getChatFormatting()), CommonColors.BLACK, ""))
                    .append(BannerBoxFont.buildMessage(
                            setInfo.cleanName(), setTier.getSecondaryColor(), CommonColors.BLACK, ""));

            Pair<Component, Component> titlePair;
            if (statsPage) {
                titlePair = Pair.of(
                        setTitle,
                        Component.literal(" (" + equippedCount + "/"
                                        + setInfo.bonuses().size() + ")")
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.GRAY)));
                linePairs.add(titlePair);

                Map<StatType, Integer> currentBonuses =
                        setInfo.bonuses().get(equippedCount).minor();

                if (currentBonuses.isEmpty()) {
                    linePairs.add(Pair.of(
                            Component.translatable(
                                            "screens.wynntils.wynntilsGuides.sets.setsButton.noBonuses"
                                                    + (equippedCount != 1 ? "Plural" : "Singular"),
                                            equippedCount)
                                    .withStyle(ChatFormatting.GRAY),
                            Component.empty()));
                } else {
                    List<StatType> sortedStats = Models.Stat.getSortedStats(
                            setInfo.bonuses().get(equippedCount).minor().keySet(), StatListOrdering.WYNNCRAFT);

                    sortedStats.forEach(stat -> {
                        int value = setInfo.bonuses().get(equippedCount).minor().get(stat);

                        MutableComponent valueComponent =
                                Component.empty().withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT));
                        CustomColor statColor = value >= 0 || stat.displayAsInverted() ? POSITIVE_STAT : NEGATIVE_STAT;

                        // No "-" needs to be appended as the value already has it
                        if (value >= 0) {
                            valueComponent.append(Component.literal("+").withColor(statColor.asInt()));
                        }

                        valueComponent.append(
                                Component.literal(String.valueOf(value)).withColor(statColor.asInt()));

                        if (stat.getUnit() != StatUnit.RAW) {
                            valueComponent.append(
                                    Component.literal(stat.getUnit().getDisplayName())
                                            .withColor(statColor.asInt()));
                        }

                        MutableComponent nameComponent = Component.empty();

                        String prefix = getIconPrefix(stat);

                        if (!prefix.isEmpty()) {
                            nameComponent.append(Component.literal(prefix)
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))
                                    .withoutShadow());
                        }

                        nameComponent.append(Component.literal(stat.getDisplayName())
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.WHITE)));

                        linePairs.add(Pair.of(nameComponent, valueComponent));
                    });
                }

                linePairs.add(Pair.of(Component.empty(), Component.empty()));
                if (equippedCount < setInfo.bonuses().size()) {
                    linePairs.add(Pair.of(
                            Component.empty()
                                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class)
                                            .withoutShadow())
                                    .append(" ")
                                    .append(Component.translatable(
                                            "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                                    + (equippedCount + 1 != 1 ? "Plural" : "Singular"),
                                            equippedCount + 1)),
                            Component.empty()));
                }
                if (equippedCount > 1) {
                    linePairs.add(Pair.of(
                            Component.empty()
                                    .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class)
                                            .withoutShadow())
                                    .append(" ")
                                    .append(Component.translatable(
                                            "screens.wynntils.wynntilsGuides.sets.setsButton.click"
                                                    + (equippedCount != 1 ? "Plural" : "Singular"),
                                            equippedCount - 1)),
                            Component.empty()));
                }
            } else {
                linePairs.add(Pair.of(setTitle, Component.empty()));
                List<Pair<Component, Component>> itemLines = new ArrayList<>();

                if (!setInfo.items().isEmpty()) {
                    setInfo.items().forEach(item -> {
                        itemLines.add(Pair.of(
                                Component.empty()
                                        .append(Component.literal(item).withStyle(ChatFormatting.WHITE))
                                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)),
                                Component.empty()));
                    });
                } else {
                    itemLines.add(Pair.of(
                            Component.literal("No items are a part of this set")
                                    .withStyle(Style.EMPTY
                                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                            .withColor(ChatFormatting.WHITE)),
                            Component.empty()));
                }

                linePairs.addAll(itemLines);
            }

            Component footer = Component.empty()
                    .withoutShadow()
                    .append(Component.literal("\uDB00\uDC30")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                    .append(Component.literal("\uE003").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP)))
                    .append(Component.literal("\uDAFF\uDF98\uDB00\uDC4B")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                    .append(Component.literal("\uE000")
                            .withStyle(Style.EMPTY
                                    .withColor((statsPage ? CURRENT_PAGE : INACTIVE_PAGE).asInt())
                                    .withFont(CommonFonts.TOOLTIP_PAGE_FONT)))
                    .append(Component.literal("\uDB00\uDC04")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                    .append(Component.literal("\uE000")
                            .withStyle(Style.EMPTY
                                    .withColor((statsPage ? INACTIVE_PAGE : CURRENT_PAGE).asInt())
                                    .withFont(CommonFonts.TOOLTIP_PAGE_FONT)));

            int widestLine = linePairs.stream()
                    .mapToInt(line -> {
                        MutableComponent component =
                                Component.empty().append(line.a()).append(line.b());
                        return McUtils.mc().font.width(component);
                    })
                    .max()
                    .orElse(0);

            List<Component> tooltipLines = new ArrayList<>();
            for (Pair<Component, Component> linePair : linePairs) {
                Component rawComponent = Component.empty().append(linePair.a()).append(linePair.b());
                int currentWidth = McUtils.mc().font.width(rawComponent);
                String space = Managers.Font.calculateOffset(currentWidth, widestLine);

                tooltipLines.add(Component.empty()
                        .append(linePair.a())
                        .append(Component.literal(space).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                        .append(linePair.b()));
            }

            int target = widestLine / 2;
            int currentWidth = McUtils.mc().font.width(footer);
            String spacing = Managers.Font.calculateOffset(currentWidth, target);
            Component paddedFooter = Component.literal(spacing)
                    .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT))
                    .append(footer);

            tooltipLines.add(paddedFooter);
            tooltipLines.add(Component.empty());

            cachedTooltip = tooltipLines;
        }

        guiGraphics.setTooltipForNextFrame(
                FontRenderer.getInstance().getFont(),
                Lists.transform(cachedTooltip, Component::getVisualOrderText),
                mouseX,
                mouseY,
                Identifier.withDefaultNamespace(setTier.getApiName()));
    }

    private static String getIconPrefix(StatType statType) {
        if (!(statType instanceof SkillStatType skillStatType)) return "";

        return switch (skillStatType.getSkill()) {
            case STRENGTH -> "\uDAFF\uDFFF\uE010\uDB00\uDC02 ";
            case DEXTERITY -> "\uE011\uDB00\uDC02 ";
            case INTELLIGENCE -> "\uDAFF\uDFFF\uE012\uDB00\uDC02 ";
            case DEFENCE -> "\uDAFF\uDFFF\uE013\uDB00\uDC01\uDB00\uDC02 ";
            case AGILITY -> "\uE014\uDB00\uDC02 ";
        };
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.NONE;
    }

    private record SetButtonState(int equippedCount, boolean statsPage) {}
}
