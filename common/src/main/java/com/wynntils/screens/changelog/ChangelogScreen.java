/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChangelogScreen extends WynntilsScreen {
    public ChangelogScreen() {
        super(Component.translatable("screens.wynntils.changelog.name"));
    }

    public static Screen create() {
        return new ChangelogScreen();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(
                (McUtils.window().getGuiScaledWidth() - Texture.CHANGELOG_BACKGROUND.width()) / 2f,
                (McUtils.window().getGuiScaledHeight() - Texture.CHANGELOG_BACKGROUND.height()) / 2f,
                0);

        RenderUtils.drawTexturedRect(poseStack, Texture.CHANGELOG_BACKGROUND, 0, 0);

        poseStack.popPose();
    }
}
