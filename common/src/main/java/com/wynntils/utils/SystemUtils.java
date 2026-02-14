/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.lwjgl.system.MemoryUtil;

public final class SystemUtils {
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    }

    public static boolean isWayland() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return (waylandDisplay != null && !waylandDisplay.isEmpty()) || ("wayland".equalsIgnoreCase(sessionType));
    }

    public static boolean usingOpenGL() {
        GpuDevice gpuDevice = RenderSystem.getDevice();

        return gpuDevice instanceof GlDevice;
    }

    public static void copyImageToClipboard(BufferedImage bi) {
        ClipboardImage ci = new ClipboardImage(bi);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ci, null);
    }

    public static CompletableFuture<NativeImage> createImage(GpuTexture texture) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();

        int textureWidth = texture.getWidth(0);
        int textureHeight = texture.getHeight(0);

        if (texture.getFormat() != TextureFormat.RGBA8) {
            throw new IllegalStateException("Tried to copy non-compatible texture into image");
        }

        GpuBuffer gpuBuffer = RenderSystem.getDevice()
                .createBuffer(
                        () -> "Wynntils SystemUtils#createImage buffer",
                        GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                        4 * textureWidth * textureHeight);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice()
                .createCommandEncoder()
                .copyTextureToBuffer(
                        texture,
                        gpuBuffer,
                        0,
                        () -> {
                            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false)) {
                                NativeImage nativeImage =
                                        new NativeImage(NativeImage.Format.RGBA, textureWidth, textureHeight, false);

                                long stride = 4L * textureWidth;
                                long srcBuf = MemoryUtil.memAddress(mappedView.data());
                                long dstBuf = nativeImage.getPointer();

                                long src = srcBuf;
                                long dst = dstBuf + stride * (textureHeight - 1);

                                for (int y = 0; y < textureHeight; y++) {
                                    MemoryUtil.memCopy(src, dst, stride);
                                    src += stride;
                                    dst -= stride;
                                }

                                future.complete(nativeImage);
                            }

                            gpuBuffer.close();
                        },
                        0);

        return future;
    }

    public static BufferedImage createScreenshot(NativeImage image) {
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                bi.setRGB(x, y, image.getPixel(x, y));
            }
        }

        return bi;
    }

    public static int getMemUsed() {
        return (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    }

    public static int getMemMax() {
        return (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
    }

    private static final class ClipboardImage implements Transferable {
        private final Image image;

        private ClipboardImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!DataFlavor.imageFlavor.equals(flavor)) throw new UnsupportedFlavorException(flavor);
            return this.image;
        }
    }
}
