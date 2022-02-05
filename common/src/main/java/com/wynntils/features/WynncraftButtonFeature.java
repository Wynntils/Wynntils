/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.utils.objects.ServerIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@FeatureInfo(
        stability = Stability.INVARIABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.SMALL)
public class WynncraftButtonFeature extends Feature {

    @SubscribeEvent
    public static void onTitleScreenInit(TitleScreenInitEvent e) {
        ServerData wynncraftServer = new ServerData("Wynncraft", "play.wynncraft.com", false);
        WynncraftButton wynncraftButton =
                new WynncraftButton(
                        e.getTitleScreen(),
                        wynncraftServer,
                        e.getTitleScreen().width / 2 + 104,
                        e.getTitleScreen().height / 4 + 48 + 24);
        e.getAddButton().accept(wynncraftButton);
    }

    private static class WynncraftButton extends Button {
        private final Screen backScreen;
        private final ServerData serverData;
        private final ServerIcon serverIcon;

        WynncraftButton(Screen backScreen, ServerData serverData, int x, int y) {
            super(x, y, 20, 20, new TranslatableComponent(""), WynncraftButton::onPress);
            this.serverData = serverData;
            this.backScreen = backScreen;

            this.serverIcon = new ServerIcon(serverData, true);
        }

        @Override
        public void renderButton(
                @NotNull PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
            super.renderButton(matrices, mouseX, mouseY, partialTicks);

            serverIcon.bind();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            // Insets the icon by 3
            blit(
                    matrices,
                    this.x + 3,
                    this.y + 3,
                    this.width - 6,
                    this.height - 6,
                    0,
                    0,
                    64,
                    64,
                    64,
                    64);
        }

        public static void onPress(Button button) {
            if (button instanceof WynncraftButton wynncraftButton) { // TODO is check necessary
                ConnectScreen.startConnecting(
                        wynncraftButton.backScreen,
                        Minecraft.getInstance(),
                        ServerAddress.parseString(wynncraftButton.serverData.ip),
                        wynncraftButton.serverData);
            }
        }
    }
}
