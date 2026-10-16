/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.screens.maps.managers.type.OverrideType;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class InfoWidget extends AbstractWidget implements TooltipProvider {
    private final CategoryManagementScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public InfoWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Info Widget"));
        this.parent = parent;
        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics, Texture.MANAGER_WIDGET_BACKGROUND_RED, getX(), getY(), this.width, this.height);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MANAGER_QUESTION_MARK_ICON,
                getX() + (this.width - Texture.MANAGER_QUESTION_MARK_ICON.width()) / 2f,
                getY() + (this.width - Texture.MANAGER_QUESTION_MARK_ICON.height()) / 2f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    public void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.title")
                        .withStyle(ChatFormatting.GOLD));

        Component description = Component.translatable(
                "screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.description");

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(description), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.literal("- ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.inherited.title")
                        .withStyle(ChatFormatting.WHITE)));

        Component inheritedDescription = Component.translatable(
                "screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.inherited.description");

        for (StyledText line :
                RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(inheritedDescription), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.literal("- ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(
                                "screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.notInherited.title")
                        .withStyle(ChatFormatting.WHITE)));

        Component notInheritedDescription = Component.translatable(
                "screens.wynntils.map.managers.categoryManager.infoWidget.inheritance.notInherited.description");

        for (StyledText line :
                RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(notInheritedDescription), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.categoryManager.infoWidget.override.title")
                        .withStyle(ChatFormatting.GOLD));

        Component overrideTypeIntro =
                Component.translatable("screens.wynntils.map.managers.categoryManager.infoWidget.override.description");

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(overrideTypeIntro), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        OverrideType currentType = parent.getSelectedOverrideType();

        OverrideType[] overrideTypes = OverrideType.values();

        for (OverrideType type : overrideTypes) {
            this.generatedTooltip.add(Component.empty());

            boolean isCurrent = type == currentType;
            ChatFormatting labelColor = isCurrent ? ChatFormatting.RED : ChatFormatting.WHITE;

            Component typeLabel = Component.literal(type.getDisplayName()).withStyle(labelColor);

            if (isCurrent) {
                typeLabel = typeLabel
                        .copy()
                        .append(" ")
                        .append(Component.translatable(
                                        "screens.wynntils.map.managers.categoryManager.infoWidget.override.current")
                                .withStyle(labelColor));
            }

            this.generatedTooltip.add(
                    Component.literal("- ").withStyle(ChatFormatting.GOLD).append(typeLabel));

            Component typeDescription =
                    switch (type) {
                        case MAP_LOCATION_OVERRIDE ->
                            Component.translatable(
                                    "screens.wynntils.map.managers.categoryManager.infoWidget.override.mapLocationOverride.description");
                        case MAP_PATH_OVERRIDE ->
                            Component.translatable(
                                    "screens.wynntils.map.managers.categoryManager.infoWidget.override.mapPathOverride.description");
                        case MAP_AREA_OVERRIDE ->
                            Component.translatable(
                                    "screens.wynntils.map.managers.categoryManager.infoWidget.override.mapAreaOverride.description");
                    };

            for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(typeDescription), 210)) {
                this.generatedTooltip.add(
                        Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
