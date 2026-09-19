/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.screens.settings.BaseWynntilsBookSettingsScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ConfigTile extends WynntilsButton {
    private final Screen screen;
    private final int maskTopY;
    private final int maskBottomY;
    private final GeneralSettingsButton resetButton;
    private final StyledText displayName;
    private final Overlay overlay;
    private AbstractWidget configOptionElement;

    public ConfigTile(int x, int y, int width, int height, Screen screen, Config<?> config, Overlay overlay) {
        super(x, y, width, height, Component.literal(config.getJsonName()));
        this.screen = screen;

        if (screen instanceof BaseWynntilsBookSettingsScreen settingsScreen) {
            maskTopY = settingsScreen.getMaskTopY();
            maskBottomY = settingsScreen.getConfigMaskBottomY();
            displayName = settingsScreen.configOptionContains(config)
                    ? StyledText.fromString(ChatFormatting.UNDERLINE + config.getDisplayName())
                    : StyledText.fromString(config.getDisplayName());
        } else if (screen instanceof OverlaySelectionScreen overlaySelectionScreen) {
            maskTopY = overlaySelectionScreen.getConfigMaskTopY();
            maskBottomY = overlaySelectionScreen.getConfigMaskBottomY();
            displayName = overlaySelectionScreen.configOptionContains(config)
                    ? StyledText.fromString(ChatFormatting.UNDERLINE + config.getDisplayName())
                    : StyledText.fromString(config.getDisplayName());
        } else {
            maskTopY = 0;
            maskBottomY = McUtils.screen().height;
            displayName = StyledText.fromString(config.getDisplayName());
        }

        this.overlay = overlay;
        this.configOptionElement = getWidgetFromConfig(config);
        this.resetButton = new ResetButton(
                config,
                () -> configOptionElement = getWidgetFromConfig(config),
                x + width - 40,
                getRenderY(),
                maskTopY,
                maskBottomY);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        resetButton.render(guiGraphics, mouseX, mouseY, partialTick);

        renderDisplayName(guiGraphics);

        RenderUtils.drawLine(
                guiGraphics,
                CommonColors.GRAY,
                this.getX(),
                this.getY() + this.height,
                this.getX() + this.width,
                this.getY() + this.height,
                1);

        configOptionElement.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void handleCursor(GuiGraphics guiGraphics) {}

    private void renderDisplayName(GuiGraphics guiGraphics) {
        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        displayName,
                        getRenderX(),
                        this.getY() + 3,
                        this.width - 3,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        0.8f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        // Prevent interaction when the tile is outside of the mask from the screen, same applies to drag and released
        if ((event.y() <= maskTopY || event.y() >= maskBottomY)) return false;

        if (McUtils.screen() instanceof BaseWynntilsBookSettingsScreen bookSettingsScreen) {
            bookSettingsScreen.changesMade();
        }

        return resetButton.mouseClicked(event, isDoubleClick) || configOptionElement.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if ((event.y() <= maskTopY || event.y() >= maskBottomY)) return false;

        return configOptionElement.mouseDragged(event, deltaX, deltaY) || super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if ((event.y() <= maskTopY || event.y() >= maskBottomY)) return false;

        return configOptionElement.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        configOptionElement.setY(getRenderY());
        resetButton.setY(getRenderY());
    }

    @Override
    public void onPress(InputWithModifiers input) {
        // noop
    }

    private static List<Component> buildTooltip(Config<?> config) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(config.getDescription()));

        List<Feature> dependents = Managers.Feature.getEnabledDependents(config, Feature.DependencyType.FUNCTIONALITY);
        if (!dependents.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("screens.wynntils.settingsScreen.dependentFeatures")
                    .withStyle(ChatFormatting.YELLOW));
            dependents.forEach(feature -> lines.add(
                    Component.literal("- " + feature.getTranslatedName()).withStyle(ChatFormatting.GRAY)));
        }

        List<Feature> customizationDependents =
                Managers.Feature.getEnabledDependents(config, Feature.DependencyType.CUSTOMIZATION);
        if (!customizationDependents.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("screens.wynntils.settingsScreen.dependentCustomizations")
                    .withStyle(ChatFormatting.GREEN));
            customizationDependents.forEach(feature -> lines.add(
                    Component.literal("- " + feature.getTranslatedName()).withStyle(ChatFormatting.GRAY)));
        }

        return ComponentUtils.wrapTooltips(lines, 250);
    }

    private int getRenderY() {
        return this.getY() + 19;
    }

    private int getRenderX() {
        return this.getX() + 3;
    }

    private <E extends Enum<E>> AbstractWidget getWidgetFromConfig(Config<?> configOption) {
        List<Component> tooltip = buildTooltip(configOption);

        // Prioritise overlay configs so that the alignment enum configs use the screen instead
        // of the normal enum widget
        if (overlay != null && screen != null) {
            if (configOption.getType().equals(OverlayPosition.class)
                    || configOption.getType().equals(OverlaySize.class)
                    || configOption.getType().equals(HorizontalAlignment.class)
                    || configOption.getType().equals(VerticalAlignment.class)) {
                return new OverlaySettingsWidget(
                        getRenderX(), getRenderY(), configOption, tooltip, screen, maskTopY, maskBottomY, overlay);
            }
        }

        if (configOption.getType().equals(Boolean.class)) {
            return new BooleanSettingsButton(
                    getRenderX(), getRenderY(), (Config<Boolean>) configOption, tooltip, maskTopY, maskBottomY);
        } else if (configOption.isEnum()) {
            return new EnumSettingsButton<>(
                    getRenderX(), getRenderY(), (Config<E>) configOption, tooltip, maskTopY, maskBottomY);
        } else if (configOption.getType().equals(CustomColor.class)) {
            return new CustomColorSettingsButton(
                    getRenderX(),
                    getRenderY(),
                    (Config<CustomColor>) configOption,
                    tooltip,
                    (TextboxScreen) screen,
                    maskTopY,
                    maskBottomY);
        }

        return new TextInputBoxSettingsWidget<>(
                getRenderX(), getRenderY(), configOption, tooltip, (TextboxScreen) screen, maskTopY, maskBottomY);
    }
}
