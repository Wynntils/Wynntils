/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.overlays.OverlaySelectionScreen;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class WynntilsMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen lastScreen;

    public WynntilsMenuScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsMenu.name"));
        lastScreen = McUtils.mc().screen;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH - 15,
                this.height / 10 + 20,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.wynntilsMenu.openOverlayMenu"),
                button -> McUtils.mc().setScreen(new OverlaySelectionScreen())));

        this.addRenderableWidget(new Button(
                this.width / 2 + 15,
                this.height / 10 + 20,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.wynntilsMenu.openSettingMenu"),
                button -> McUtils.mc().setScreen(new WynntilsSettingsScreen())));

        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height / 10 + BUTTON_HEIGHT + 30,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.wynntilsMenu.close"),
                button -> McUtils.mc().setScreen(lastScreen)));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsMenu.title"),
                        this.width / 2f,
                        this.height / 10f,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);
    }
}
