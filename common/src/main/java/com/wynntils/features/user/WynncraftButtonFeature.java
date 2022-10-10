/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.objects.ServerIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.INVARIABLE)
public class WynncraftButtonFeature extends UserFeature {
    @SubscribeEvent
    public void onTitleScreenInit(TitleScreenInitEvent e) {
        ServerData wynncraftServer = new ServerData("Wynncraft", "play.wynncraft.com", false);
        wynncraftServer.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);

        WynncraftButton wynncraftButton = new WynncraftButton(
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

        // TODO tooltip
        WynncraftButton(Screen backScreen, ServerData serverData, int x, int y) {
            super(x, y, 20, 20, new TranslatableComponent(""), WynncraftButton::onPress);
            this.serverData = serverData;
            this.backScreen = backScreen;

            this.serverIcon = new ServerIcon(serverData);
            this.serverIcon.loadResource(false);
        }

        @Override
        public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
            super.renderButton(matrices, mouseX, mouseY, partialTicks);

            if (serverIcon == null || serverIcon.getServerIconLocation() == null) {
                return;
            }

            RenderSystem.setShaderTexture(0, serverIcon.getServerIconLocation());

            // Insets the icon by 3
            blit(matrices, this.x + 3, this.y + 3, this.width - 6, this.height - 6, 0, 0, 64, 64, 64, 64);
        }

        protected static void onPress(Button button) {
            if (!(button instanceof WynncraftButton wynncraftButton)) return;

            ConnectScreen.startConnecting(
                    wynncraftButton.backScreen,
                    Minecraft.getInstance(),
                    ServerAddress.parseString(wynncraftButton.serverData.ip),
                    wynncraftButton.serverData);
        }
    }
}
