/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import java.util.Set;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class OverlayManagementScreen extends Screen {
    public OverlayManagementScreen() {
        super(new TranslatableComponent("screens.wynntils.overlayManagement.name"));
    }

    @Override
    protected void init() {
        setupButtons();
    }

    private void setupButtons() {
        final int BUTTON_WIDTH = 60;
        final int BUTTON_HEIGHT = 20;

        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH * 2,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.closeSettingsScreen"),
                button -> {
                    McUtils.mc().setScreen(null);
                }));
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.testSettings"),
                button -> {}));
        this.addRenderableWidget(new Button(
                this.width / 2 + BUTTON_WIDTH,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.applySettings"),
                button -> {
                    ConfigManager.saveConfig();
                    McUtils.mc().setScreen(null);
                }));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        Set<Overlay> overlays = OverlayManager.getOverlays();

        for (Overlay overlay : overlays) {
            CustomColor color = OverlayManager.isEnabled(overlay) ? CommonColors.GREEN : CommonColors.RED;
            RenderUtils.drawRectBorders(
                    poseStack,
                    color,
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    overlay.getRenderX() + overlay.getWidth(),
                    overlay.getRenderY() + overlay.getHeight(),
                    1,
                    1.8f);
            RenderUtils.drawRect(
                    poseStack,
                    color.withAlpha(30),
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    0,
                    overlay.getWidth(),
                    overlay.getHeight());
        }

        super.render(poseStack, mouseX, mouseY, partialTick); // This renders widgets
    }
}
