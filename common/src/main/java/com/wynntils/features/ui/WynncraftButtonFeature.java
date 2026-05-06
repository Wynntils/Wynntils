/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.mc.event.TitleScreenRebuildEvent;
import com.wynntils.models.worlds.type.ServerRegion;
import com.wynntils.screens.downloads.DownloadScreen;
import com.wynntils.screens.update.UpdateScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class WynncraftButtonFeature extends Feature {
    private static final String WYNNCRAFT_DOMAIN = ".wynncraft.com";
    private boolean firstTitleScreenInit = true;
    private boolean hasUsedButton = false;

    @Persisted
    private final Config<ServerType> serverType = new Config<>(ServerType.GAME);

    @Persisted
    private final Config<ServerRegion> serverRegionOverride = new Config<>(ServerRegion.WC);

    @Persisted
    private final Config<Boolean> autoConnect = new Config<>(false);

    @Persisted
    private final Config<Boolean> loadResourcePack = new Config<>(true);

    @Persisted
    private final Config<Boolean> cancelAutoJoin = new Config<>(true);

    @Persisted
    public final Storage<Boolean> ignoreFailedDownloads = new Storage<>(false);

    @Persisted
    private final Config<Boolean> returnToTitle = new Config<>(true);

    public WynncraftButtonFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onTitleScreenInitPre(TitleScreenInitEvent.Pre event) {
        if (!firstTitleScreenInit || !autoConnect.get()) return;

        firstTitleScreenInit = false;
        if (Managers.Download.graphState().error() && cancelAutoJoin.get() && !ignoreFailedDownloads.get()) {
            WynntilsMod.warn("Downloads have failed, auto join is cancelled.");
            return;
        } else if (Services.Update.shouldPromptUpdate() && cancelAutoJoin.get()) {
            WynntilsMod.info("Cancelling auto join, update available");
            return;
        }

        ServerData wynncraftServer = getWynncraftServer();
        connectToServer(wynncraftServer);
    }

    @SubscribeEvent
    public void onTitleScreenInitPost(TitleScreenInitEvent.Post event) {
        addWynncraftButton(event.getTitleScreen());
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenOpenedEvent.Pre event) {
        if (!hasUsedButton) return;

        if (event.getScreen() instanceof TitleScreen) {
            hasUsedButton = false;
            return;
        }
        if (event.getScreen() instanceof JoinMultiplayerScreen) {
            hasUsedButton = false;
            if (returnToTitle.get()) {
                event.setCanceled(true);
                McUtils.setScreen(new TitleScreen());
            }
        }
    }

    @SubscribeEvent
    public void onTitleScreenRebuild(TitleScreenRebuildEvent.Post event) {
        addWynncraftButton(event.getTitleScreen());
    }

    private void addWynncraftButton(TitleScreen titleScreen) {
        if (titleScreen.children.stream().anyMatch(child -> child instanceof WynncraftButton)) return;

        ServerData wynncraftServer = getWynncraftServer();
        WarningType warningType = WarningType.NONE;

        if (Managers.Download.graphState().error()) {
            warningType = WarningType.DOWNLOADS;
        } else if (Services.Update.shouldPromptUpdate()) {
            warningType = WarningType.UPDATE;
        }

        WynncraftButton wynncraftButton = new WynncraftButton(
                titleScreen,
                wynncraftServer,
                titleScreen.width / 2 + 104,
                titleScreen.height / 4 + 48 + 24,
                warningType,
                ignoreFailedDownloads.get(),
                this::onPress);
        titleScreen.addRenderableWidget(wynncraftButton);
    }

    private ServerData getWynncraftServer() {
        String ip = (serverType.get() == ServerType.GAME && serverRegionOverride.get() != ServerRegion.WC
                        ? serverRegionOverride.get().name().toLowerCase(Locale.ROOT)
                        : serverType.get().serverAddressPrefix)
                + WYNNCRAFT_DOMAIN;
        ServerData wynncraftServer = new ServerData("Wynncraft", ip, ServerData.Type.OTHER);
        wynncraftServer.setResourcePackStatus(
                loadResourcePack.get() ? ServerData.ServerPackStatus.ENABLED : ServerData.ServerPackStatus.DISABLED);

        return wynncraftServer;
    }

    private void connectToServer(ServerData serverData) {
        hasUsedButton = true;
        ConnectScreen.startConnecting(
                McUtils.screen(), McUtils.mc(), ServerAddress.parseString(serverData.ip), serverData, false, null);
    }

    private void onPress(Button button) {
        if (!(button instanceof WynncraftButton wynncraftButton)) return;
        if (!Managers.Download.graphState().finished()) return;

        if (wynncraftButton.warningType == WarningType.UPDATE) {
            McUtils.setScreen(UpdateScreen.create(wynncraftButton.serverData, wynncraftButton.titleScreen));
        } else if (wynncraftButton.warningType == WarningType.DOWNLOADS && !wynncraftButton.ignoreFailedDownloads) {
            McUtils.setScreen(DownloadScreen.create(McUtils.screen(), wynncraftButton.serverData));
        } else {
            connectToServer(wynncraftButton.serverData);
        }
    }

    private static class WynncraftButton extends Button.Plain {
        // Keep using the bundled Wynncraft icon until dynamic server-icon loading is restored.
        private static final Identifier BUTTON_ICON = Texture.WYNNCRAFT_ICON.identifier();
        private static final List<Component> CONNECT_TOOLTIP =
                List.of(Component.translatable("feature.wynntils.wynncraftButton.connect"));
        private static final List<Component> DOWNLOAD_TOOLTIP = List.of(
                Component.translatable("feature.wynntils.wynncraftButton.download1"),
                Component.translatable("feature.wynntils.wynncraftButton.download2"));
        private static final List<Component> UPDATE_TOOLTIP =
                List.of(Component.translatable("feature.wynntils.wynncraftButton.update"));
        private final Screen titleScreen;
        private final ServerData serverData;
        private final WarningType warningType;
        private final boolean ignoreFailedDownloads;
        private final List<Component> tooltip;

        WynncraftButton(
                Screen titleScreen,
                ServerData serverData,
                int x,
                int y,
                WarningType warningType,
                boolean ignoreFailedDownloads,
                OnPress onPress) {
            super(x, y, 20, 20, Component.literal(""), onPress, Button.DEFAULT_NARRATION);
            this.serverData = serverData;
            this.titleScreen = titleScreen;
            this.warningType = warningType;
            this.ignoreFailedDownloads = ignoreFailedDownloads;

            if (warningType == WarningType.DOWNLOADS) {
                tooltip = DOWNLOAD_TOOLTIP;
            } else if (warningType == WarningType.UPDATE) {
                tooltip = UPDATE_TOOLTIP;
            } else {
                tooltip = CONNECT_TOOLTIP;
            }
        }

        @Override
        public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderContents(guiGraphics, mouseX, mouseY, partialTicks);
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    BUTTON_ICON,
                    CommonColors.WHITE.withAlpha(this.alpha),
                    this.getX() + 3,
                    this.getY() + 3,
                    this.width - 6,
                    this.height - 6,
                    64,
                    64);

            if (warningType == WarningType.DOWNLOADS) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString("⚠"),
                                this.getX() + 20,
                                this.getY(),
                                CommonColors.RED.withAlpha(this.alpha),
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.OUTLINE);
            } else if (warningType == WarningType.UPDATE) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromString("⟳"),
                                this.getX() + 2,
                                this.getY(),
                                CommonColors.YELLOW.withAlpha(this.alpha),
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.OUTLINE,
                                1.5f);
            }

            if (isHovered) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(tooltip, Component::getVisualOrderText), mouseX, mouseY);
            }
        }
    }

    private enum ServerType {
        LOBBY("lobby"),
        GAME("play"),
        MEDIA("media"),
        BETA("beta");

        private final String serverAddressPrefix;

        ServerType(String serverAddressPrefix) {
            this.serverAddressPrefix = serverAddressPrefix;
        }
    }

    private enum WarningType {
        NONE,
        DOWNLOADS,
        UPDATE
    }
}
