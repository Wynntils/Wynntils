/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Services;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.screens.maps.managers.widgets.ProviderEntryWidget;
import com.wynntils.services.mapdata.providers.MapDataProviderInfo;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ProviderManagerEntryWidget extends ProviderEntryWidget {
    private static final int BUTTON_PADDING = 5;

    private final ProviderType providerType;
    private final boolean toggleable;

    public boolean enabled;

    private ProviderManagerEntryWidget(
            String completeProviderId, Component name, ProviderType providerType, ProviderManagementScreen parent) {
        super(
                completeProviderId,
                name,
                parent::getSelectedCompleteProviderId,
                id -> parent.setSelectedProvider(id, providerType));
        this.providerType = providerType;
        this.enabled = Services.MapData.isProviderEnabled(completeProviderId);

        MapDataProviderInfo info = Services.MapData.getProviderInfos().get(completeProviderId);
        this.toggleable = info != null && info.toggleable();
    }

    public static ProviderManagerEntryWidget createBuiltInProviderEntry(
            BuiltInProvider provider, ProviderManagementScreen parent) {
        return new ProviderManagerEntryWidget(
                "built-in:" + provider.getProviderId(), provider.getName(), ProviderType.BUILT_IN, parent);
    }

    public static ProviderManagerEntryWidget createJsonProviderEntry(
            JsonProviderInfo provider, ProviderType providerType, ProviderManagementScreen parent) {
        return new ProviderManagerEntryWidget(
                provider.completeId(), Component.literal(provider.providerId()), providerType, parent);
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    @Override
    protected int getTextMaxWidth() {
        return toggleable
                ? super.getTextMaxWidth() - getButtonTexture().width() - BUTTON_PADDING
                : super.getTextMaxWidth();
    }

    @Override
    protected void renderExtras(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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

            if (Services.MapData.toggleProvider(getProviderId())) {
                enabled = Services.MapData.isProviderEnabled(getProviderId());
            }

            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    private Texture getButtonTexture() {
        return enabled ? Texture.MANAGER_TOGGLE_BUTTON_ON : Texture.MANAGER_TOGGLE_BUTTON_OFF;
    }

    private int getButtonX() {
        return getX() + getWidth() - getButtonTexture().width() - BUTTON_PADDING;
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
