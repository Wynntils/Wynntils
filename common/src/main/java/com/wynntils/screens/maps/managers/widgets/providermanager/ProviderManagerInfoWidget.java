/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
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

public class ProviderManagerInfoWidget extends AbstractWidget implements TooltipProvider {
    private final ProviderManagementScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public ProviderManagerInfoWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
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

        // Overview
        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.providerManager.infoWidget.overview.title")
                        .withStyle(ChatFormatting.GOLD));

        Component overviewDescription = Component.translatable(
                "screens.wynntils.map.managers.providerManager.infoWidget.overview.description");

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(overviewDescription), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        this.generatedTooltip.add(Component.empty());

        // Provider types
        this.generatedTooltip.add(
                Component.translatable("screens.wynntils.map.managers.providerManager.infoWidget.types.title")
                        .withStyle(ChatFormatting.GOLD));

        Component typesDescription = Component.translatable(
                "screens.wynntils.map.managers.providerManager.infoWidget.types.description");

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(typesDescription), 210)) {
            this.generatedTooltip.add(
                    Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
        }

        ProviderType currentType = parent.getSelectedProviderType();

        for (ProviderType type : ProviderType.values()) {
            this.generatedTooltip.add(Component.empty());

            boolean isCurrent = type == currentType;
            ChatFormatting labelColor = isCurrent ? ChatFormatting.RED : ChatFormatting.WHITE;

            Component typeLabel = Component.literal(type.getDisplayName()).withStyle(labelColor);

            if (isCurrent) {
                typeLabel = typeLabel
                        .copy()
                        .append(" ")
                        .append(Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.infoWidget.types.current")
                                .withStyle(labelColor));
            }

            this.generatedTooltip.add(
                    Component.literal("- ").withStyle(ChatFormatting.GOLD).append(typeLabel));

            Component typeDescription =
                    switch (type) {
                        case BUILT_IN ->
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.infoWidget.types.builtIn.description");
                        case JSON_BUNDLED ->
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.infoWidget.types.jsonBundled.description");
                        case JSON_LOCAL ->
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.infoWidget.types.jsonLocal.description");
                        case JSON_REMOTE ->
                                Component.translatable(
                                        "screens.wynntils.map.managers.providerManager.infoWidget.types.jsonRemote.description");
                    };

            for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(typeDescription), 210)) {
                this.generatedTooltip.add(
                        Component.empty().append(line.getComponent()).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}