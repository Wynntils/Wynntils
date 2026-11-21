/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.impl;

import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class MapIconImpl implements MapIcon {
    // Icons come from user input and from providers, which can be remote, so their size has to be limited. Otherwise
    // a single huge image could use up all the memory when it is decoded and uploaded as a texture
    public static final int MAX_ICON_SIZE = 64;
    public static final int MAX_TEXTURE_BYTES = 64 * 1024;
    public static final int MAX_BASE64_LENGTH = (MAX_TEXTURE_BYTES + 2) / 3 * 4;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final int PNG_IHDR_TYPE_OFFSET = 12;
    private static final int PNG_WIDTH_OFFSET = 16;
    private static final int PNG_HEIGHT_OFFSET = 20;
    private static final int PNG_HEADER_LENGTH = 24;

    private final String iconId;
    private final NativeImage nativeImage;
    private final byte[] texture;
    private final int width;
    private final int height;
    private final Identifier resource;

    private boolean registered;

    public MapIconImpl(String iconId, byte[] texture) throws IOException {
        // This has to be done before decoding, as it is the decoding that allocates the memory for the whole image
        validateTexture(texture);

        this.iconId = iconId;
        this.nativeImage = NativeImage.read(texture);
        if (nativeImage.getWidth() > MAX_ICON_SIZE || nativeImage.getHeight() > MAX_ICON_SIZE) {
            int decodedWidth = nativeImage.getWidth();
            int decodedHeight = nativeImage.getHeight();
            nativeImage.close();
            throw new IconTooLargeException("Icon is " + decodedWidth + "x" + decodedHeight + ", the maximum is "
                    + MAX_ICON_SIZE + "x" + MAX_ICON_SIZE);
        }

        // It's hard to get the bytes back from the native image, so we store them here
        this.texture = texture;
        this.width = nativeImage.getWidth();
        this.height = nativeImage.getHeight();
        this.resource = Identifier.fromNamespaceAndPath("wynntils", "icons/" + iconId.replace(":", "."));
    }

    /**
     * Checks that the texture is a PNG image that is within the size limits, by only looking at its header.
     *
     * @throws IconTooLargeException if the file or the image is larger than what is allowed
     * @throws IOException if the texture is not a PNG image
     */
    public static void validateTexture(byte[] texture) throws IOException {
        if (texture.length > MAX_TEXTURE_BYTES) {
            throw new IconTooLargeException(
                    "Icon is " + texture.length + " bytes, the maximum is " + MAX_TEXTURE_BYTES + " bytes");
        }

        if (texture.length < PNG_HEADER_LENGTH
                || !Arrays.equals(texture, 0, PNG_SIGNATURE.length, PNG_SIGNATURE, 0, PNG_SIGNATURE.length)
                || texture[PNG_IHDR_TYPE_OFFSET] != 'I'
                || texture[PNG_IHDR_TYPE_OFFSET + 1] != 'H'
                || texture[PNG_IHDR_TYPE_OFFSET + 2] != 'D'
                || texture[PNG_IHDR_TYPE_OFFSET + 3] != 'R') {
            throw new IOException("Icon is not a PNG image");
        }

        ByteBuffer header = ByteBuffer.wrap(texture);
        int headerWidth = header.getInt(PNG_WIDTH_OFFSET);
        int headerHeight = header.getInt(PNG_HEIGHT_OFFSET);

        if (headerWidth <= 0 || headerHeight <= 0) {
            throw new IOException("Icon has an invalid size");
        }

        if (headerWidth > MAX_ICON_SIZE || headerHeight > MAX_ICON_SIZE) {
            throw new IconTooLargeException("Icon is " + headerWidth + "x" + headerHeight + ", the maximum is "
                    + MAX_ICON_SIZE + "x" + MAX_ICON_SIZE);
        }
    }

    @Override
    public Identifier getIdentifier() {
        if (!registered) {
            // We cannot do this in the constructor since GL is not initiated at that time
            registered = true;
            McUtils.mc().getTextureManager().register(resource, new DynamicTexture(() -> iconId, nativeImage));
        }

        return resource;
    }

    @Override
    public String getIconId() {
        return iconId;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public byte[] getTextureBytes() {
        return texture;
    }

    public static final class IconTooLargeException extends IOException {
        private IconTooLargeException(String message) {
            super(message);
        }
    }
}
