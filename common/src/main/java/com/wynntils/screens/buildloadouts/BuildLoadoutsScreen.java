/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts;


import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.screens.activities.ContentBookHolder;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BuildLoadoutsScreen extends WynntilsScreen {
    private int offsetX;
    private int offsetY;

    private BuildLoadoutsScreen() {
        super(Component.literal("Build Loadouts Screen"));
    }

    public static Screen create() {
        return new BuildLoadoutsScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.BUILD_LOADOUTS_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.BUILD_LOADOUTS_BACKGROUND.height()) / 2f);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(guiGraphics);
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND,
                offsetX+20,
                offsetY+20,
                250,
                150,
                6,
                6,
                6,
                6);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                offsetX+30,
                offsetY+30,
                230,
                130,
                6,
                6,
                6,
                6);
    }



    private void renderBackgroundTexture(GuiGraphics guiGraphics) {
        RenderUtils.drawTexturedRect(guiGraphics, Texture.BUILD_LOADOUTS_BACKGROUND, offsetX, offsetY);
    }


}
