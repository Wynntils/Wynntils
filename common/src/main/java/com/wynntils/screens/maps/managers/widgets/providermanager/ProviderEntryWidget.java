/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.services.mapdata.providers.MapDataProviderInfo;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderEntryWidget extends AbstractProviderWidget {
    private static final int PADDING = 5;

    private final ProviderManagementScreen parent;

    public boolean enabled;

    private final BuiltInProvider builtInProvider;
    private final JsonProviderInfo jsonProviderInfo;
    private final String providerId;
    private final String completeProviderId;
    private final Component name;
    private final boolean toggleable;

    private ProviderEntryWidget(
            BuiltInProvider builtInProvider,
            JsonProviderInfo jsonProviderInfo,
            ProviderType providerType,
            ProviderManagementScreen parent) {
        super(Component.literal("Provider Entry Widget"), 28, providerType);
        this.builtInProvider = builtInProvider;
        this.jsonProviderInfo = jsonProviderInfo;
        this.parent = parent;

        this.providerId = builtInProvider != null ? builtInProvider.getProviderId() : jsonProviderInfo.providerId();
        this.completeProviderId =
                builtInProvider != null ? "built-in:" + builtInProvider.getProviderId() : jsonProviderInfo.completeId();

        this.name = builtInProvider != null ? builtInProvider.getName() : Component.literal(this.providerId);
        this.enabled = Services.MapData.isProviderEnabled(this.completeProviderId);

        MapDataProviderInfo info = Services.MapData.getProviderInfos().get(this.completeProviderId);
        this.toggleable = info != null && info.toggleable();

        // Used for searching.
        message = this.name;
    }

    public static ProviderEntryWidget createBuiltInProviderEntry(
            BuiltInProvider provider, ProviderManagementScreen parent) {
        return new ProviderEntryWidget(provider, null, ProviderType.BUILT_IN, parent);
    }

    public static ProviderEntryWidget createJsonProviderEntry(
            JsonProviderInfo provider, ProviderType providerType, ProviderManagementScreen parent) {
        return new ProviderEntryWidget(null, provider, providerType, parent);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                !Objects.equals(parent.getSelectedProviderId(), providerId)
                        ? Texture.MANAGER_WIDGET_BACKGROUND
                        : Texture.MANAGER_WIDGET_BACKGROUND_RED,
                getX(),
                getY(),
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(name),
                        getX() + PADDING,
                        getY() + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (toggleable) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    getButtonTexture(),
                    getButtonX(),
                    getY() + (this.height - getButtonTexture().height()) / 2f);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;

        if (toggleable && isMouseInsideButton(event.x(), event.y())) {
            this.playDownSound(McUtils.mc().getSoundManager());

            if (Services.MapData.toggleProvider(completeProviderId)) {
                enabled = Services.MapData.isProviderEnabled(completeProviderId);
            }

            return true;
        } else {
            this.playDownSound(McUtils.mc().getSoundManager());

            parent.setSelectedProvider(providerId, providerType);
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private Texture getButtonTexture() {
        return enabled ? Texture.MANAGER_TOGGLE_BUTTON_ON : Texture.MANAGER_TOGGLE_BUTTON_OFF;
    }

    private int getButtonX() {
        return getX() + getWidth() - getButtonTexture().width() - PADDING;
    }

    private boolean isMouseInsideButton(double mouseX, double mouseY) {
        int buttonX = getButtonX();
        int buttonY = getY() + (this.height - getButtonTexture().height()) / 2;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                buttonX,
                buttonX + getButtonTexture().width(),
                buttonY,
                buttonY + getButtonTexture().height());
    }
}
