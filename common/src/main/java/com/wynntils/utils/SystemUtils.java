/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.util.Locale;

public final class SystemUtils {
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    }

    public static boolean isWayland() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return (waylandDisplay != null && !waylandDisplay.isEmpty()) || ("wayland".equalsIgnoreCase(sessionType));
    }

    public static void copyImageToClipboard(BufferedImage bi) {
        ClipboardImage ci = new ClipboardImage(bi);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ci, null);
    }

    public static BufferedImage createScreenshot(RenderTarget fb) {
        BufferedImage bufferedimage = new BufferedImage(fb.width, fb.height, BufferedImage.TYPE_INT_ARGB);
        try (NativeImage image = new NativeImage(fb.width, fb.height, false)) {
            RenderSystem.bindTexture(fb.getColorTextureId());
            image.downloadTexture(0, false);
            image.flipY();

            int[] pixelValues = image.getPixels();

            bufferedimage.setRGB(0, 0, fb.width, fb.height, pixelValues, 0, fb.width);
        }
        return bufferedimage;
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
