/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.event.TitleScreenInitEvent;
import com.wynntils.models.worlds.type.ServerRegion;
import com.wynntils.screens.downloads.DownloadScreen;
import com.wynntils.screens.update.UpdateScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.Validate;

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

    @SubscribeEvent
    public void onTitleScreenInit(TitleScreenInitEvent.Post event) {
        TitleScreen titleScreen = event.getTitleScreen();

        addWynncraftButton(titleScreen);
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
    public void onScreenInit(ScreenInitEvent.Pre event) {
        if (!(event.getScreen() instanceof TitleScreen titleScreen)) return;

        if (firstTitleScreenInit && autoConnect.get()) {
            firstTitleScreenInit = false;
            if (Managers.Download.graphState().error() && cancelAutoJoin.get() && !ignoreFailedDownloads.get()) {
                WynntilsMod.warn("Downloads have failed, auto join is cancelled.");
                return;
            } else if (Services.Update.shouldPromptUpdate()) {
                WynntilsMod.info("Cancelling auto join, update available");
                return;
            }
            ServerData wynncraftServer = getWynncraftServer();
            connectToServer(wynncraftServer);
            return;
        }

        addWynncraftButton(titleScreen);
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

    private static class WynncraftButton extends Button {
        private static final List<Component> CONNECT_TOOLTIP =
                List.of(Component.translatable("feature.wynntils.wynncraftButton.connect"));
        private static final List<Component> DOWNLOAD_TOOLTIP = List.of(
                Component.translatable("feature.wynntils.wynncraftButton.download1"),
                Component.translatable("feature.wynntils.wynncraftButton.download2"));
        private static final List<Component> UPDATE_TOOLTIP =
                List.of(Component.translatable("feature.wynntils.wynncraftButton.update"));
        private final Screen titleScreen;
        private final ServerData serverData;
        private final ServerIcon serverIcon;
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

            this.serverIcon = new ServerIcon(serverData);
            this.serverIcon.loadResource(false);
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
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

            if (serverIcon == null || serverIcon.getServerIconLocation() == null) {
                return;
            }

            // Insets the icon by 3
            BufferedRenderUtils.drawScalingTexturedRect(
                    guiGraphics.pose(),
                    guiGraphics.bufferSource,
                    serverIcon.getServerIconLocation(),
                    this.getX() + 3,
                    this.getY() + 3,
                    0,
                    this.width - 6,
                    this.height - 6,
                    64,
                    64);

            if (warningType == WarningType.DOWNLOADS) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics.pose(),
                                StyledText.fromString("⚠"),
                                this.getX() + 20,
                                this.getY(),
                                CommonColors.RED,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.OUTLINE);
            } else if (warningType == WarningType.UPDATE) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics.pose(),
                                StyledText.fromString("⟳"),
                                this.getX() + 2,
                                this.getY(),
                                CommonColors.YELLOW,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.OUTLINE,
                                1.5f);
            }

            if (isHovered) {
                McUtils.screen().setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
            }
        }
    }

    /**
     * Provides the icon for a server in the form of a {@link ResourceLocation} with utility methods
     */
    private static final class ServerIcon {
        private static final ResourceLocation FALLBACK;

        private final ServerData server;
        private ResourceLocation serverIconLocation;
        private final Consumer<ServerIcon> onDone;

        static {
            FALLBACK = Texture.WYNNCRAFT_ICON.resource();
        }

        /**
         * @param server {@link ServerData} of server
         * @param onDone consumer when done, can be null if none
         */
        private ServerIcon(ServerData server, Consumer<ServerIcon> onDone) {
            this.server = server;
            this.onDone = onDone;
            this.serverIconLocation = FALLBACK;
        }

        private void loadResource(boolean allowStale) {
            // Try default
            @SuppressWarnings("deprecation")
            ResourceLocation destination = ResourceLocation.withDefaultNamespace(
                    "servers/" + Hashing.sha1().hashUnencodedChars(server.ip) + "/icon");

            // If someone converts this to get the actual ServerData used by the gui, check
            // ServerData#pinged here and
            // set it later
            if (allowStale && McUtils.mc().getTextureManager().getTexture(destination) != null) {
                serverIconLocation = destination;
                onDone();
                return;
            }

            try {
                ServerStatusPinger pinger = new ServerStatusPinger();
                // FIXME: DynamicTexture issues in loadServerIcon
                //        loadServerIcon(destination);
                pinger.pingServer(server, () -> {}, this::onDone);
            } catch (Exception e) {
                WynntilsMod.warn("Failed to ping server", e);
                onDone();
            }
        }

        private ServerIcon(ServerData server) {
            this(server, null);
        }

        /**
         * Returns whether getting the icon has succeeded.
         */
        public boolean isSuccess() {
            return !FALLBACK.equals(serverIconLocation);
        }

        /**
         * Returns the {@link ServerData} used to get the icon
         */
        public ServerData getServer() {
            return server;
        }

        /**
         * Returns the icon as a {@link ResourceLocation} if found, else unknown server texture
         */
        private synchronized ResourceLocation getServerIconLocation() {
            return serverIconLocation;
        }

        private void onDone() {
            if (onDone != null) onDone.accept(this);
        }

        // Modified from
        // net.minecraft.client.gui.screens.multiplayer.ServerSelectionList#uploadServerIcon
        private synchronized void loadServerIcon(ResourceLocation destination) {
            byte[] iconBytes = server.getIconBytes();
            if (iconBytes == null) {
                // failed to ping server or icon wasn't sent
                WynntilsMod.warn("Unable to load icon");
                serverIconLocation = FALLBACK;
                return;
            }
            ByteBuffer iconBytesBuffer = ByteBuffer.wrap(iconBytes);

            try (NativeImage nativeImage = NativeImage.read(iconBytesBuffer)) {
                Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");

                synchronized (this) {
                    RenderSystem.recordRenderCall(() -> {
                        McUtils.mc().getTextureManager().register(destination, new DynamicTexture(nativeImage));
                        serverIconLocation = destination;
                    });
                }
            } catch (IOException e) {
                WynntilsMod.error("Unable to read server image: " + server.name, e);
                serverIconLocation = FALLBACK;
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
