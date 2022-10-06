/*
 * Copyright © Wynntils 2018-2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.objects;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.WynntilsMod;
import com.wynntils.gui.render.Texture;
import java.io.IOException;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/** Provides the icon for a server in the form of a {@link ResourceLocation} with utility methods */
public class ServerIcon {
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

    public void loadResource(boolean allowStale) {
        // Try default
        ResourceLocation destination =
                new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(server.ip) + "/icon");

        // If someone converts this to get the actual ServerData used by the gui, check
        // ServerData#pinged here and
        // set it later
        if (allowStale && Minecraft.getInstance().getTextureManager().getTexture(destination, null) != null) {
            serverIconLocation = destination;
            onDone();
            return;
        }

        try {
            ServerStatusPinger pinger = new ServerStatusPinger();
            pinger.pingServer(server, () -> {
                // FIXME: DynamicTexture issues in loadServerIcon
                //        loadServerIcon(destination);
                onDone();
            });
        } catch (Exception e) {
            WynntilsMod.warn("Failed to ping server", e);
            onDone();
        }
    }

    public ServerIcon(ServerData server) {
        this(server, null);
    }

    /** Returns whether getting the icon has succeeded. */
    public boolean isSuccess() {
        return !FALLBACK.equals(serverIconLocation);
    }

    /** Returns the {@link ServerData} used to get the icon */
    public ServerData getServer() {
        return server;
    }

    /** Returns the icon as a {@link ResourceLocation} if found, else unknown server texture */
    public synchronized ResourceLocation getServerIconLocation() {
        return serverIconLocation;
    }

    private void onDone() {
        if (onDone != null) onDone.accept(this);
    }

    // Modified from
    // net.minecraft.client.gui.screens.multiplayer.ServerSelectionList#uploadServerIcon
    private synchronized void loadServerIcon(ResourceLocation destination) {
        String iconString = server.getIconB64();
        // failed to ping server or icon wasn't sent
        if (iconString == null) {
            WynntilsMod.warn("Unable to load icon");
            serverIconLocation = FALLBACK;
            return;
        }

        try {
            try (NativeImage nativeImage = NativeImage.fromBase64(iconString)) {
                Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");

                synchronized (this) {
                    RenderSystem.recordRenderCall(() -> {
                        Minecraft.getInstance()
                                .getTextureManager()
                                .register(destination, new DynamicTexture(nativeImage));
                        serverIconLocation = destination;
                    });
                }
            }
        } catch (IOException e) {
            WynntilsMod.error("Unable to convert image from base64: " + iconString, e);
            serverIconLocation = FALLBACK;
        }
    }
}
