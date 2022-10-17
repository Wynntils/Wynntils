/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.utils.ComponentUtils;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SkillPointLoadoutsFeature extends UserFeature {
    private static final String CHARACTER_MENU_TITLE = "Character Info";

    private static final List<Component> SAVE_TOOLTIP =
            List.of(new TranslatableComponent("feature.wynntils.skillPointLoadouts.saveTooltip"));
    private static final List<Component> LOAD_TOOLTIP =
            List.of(new TranslatableComponent("feature.wynntils.skillPointLoadouts.loadTooltip"));

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent event) {
        Screen screen = event.getScreen();
        String title = ComponentUtils.getUnformatted(screen.getTitle());

        if (!CHARACTER_MENU_TITLE.equals(title)) return;

        if (!(screen instanceof ContainerScreen containerScreen)) return;

        float leftPos = (containerScreen.width - containerScreen.imageWidth) / 2f;
        float topPos = (containerScreen.height - containerScreen.imageHeight) / 2f;

        screen.addRenderableWidget(new Button(
                (int) (leftPos + containerScreen.imageWidth / 3f) - 18,
                (int) topPos - 25,
                18,
                19,
                new TextComponent("S"),
                button -> {},
                this::saveButtonHover));
        screen.addRenderableWidget(new Button(
                (int) (leftPos + containerScreen.imageWidth / 3f * 2f),
                (int) topPos - 25,
                18,
                19,
                new TextComponent("L"),
                button -> {},
                this::loadButtonHover));
    }

    private void saveButtonHover(Button button, PoseStack poseStack, int x, int y) {
        RenderUtils.drawTooltipAt(
                poseStack, x, y, 200, SAVE_TOOLTIP, FontRenderer.getInstance().getFont(), true);
    }

    private void loadButtonHover(Button button, PoseStack poseStack, int x, int y) {
        RenderUtils.drawTooltipAt(
                poseStack, x, y, 200, LOAD_TOOLTIP, FontRenderer.getInstance().getFont(), true);
    }
}
