/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.features.utilities.SoundTriggersFeature;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SoundTriggerManagmentScreen extends WynntilsScreen {
    private final Texture BACKGROUND_TEXTURE = Texture.OVERLAY_SELECTION_GUI;

    private final Screen previousScreen;
    private final Feature soundTriggerFeature;

    private SoundTriggerManagmentScreen(Screen previousScreen) {
        super(Component.literal("Sound Triggers Managment Screen"));
        this.previousScreen = previousScreen;
        this.soundTriggerFeature = Managers.Feature.getFeatureInstance(SoundTriggersFeature.class);
    }

    public static Screen screen(Screen previousScreen) {
        return new SoundTriggerManagmentScreen(previousScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderUtils.drawTexturedRect(guiGraphics, BACKGROUND_TEXTURE, getTranslationX(), getTranslationY());
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    private float getTranslationX() {
        return (this.width - BACKGROUND_TEXTURE.width()) / 2f;
    }

    private float getTranslationY() {
        return (this.height - BACKGROUND_TEXTURE.height()) / 2f;
    }
}
