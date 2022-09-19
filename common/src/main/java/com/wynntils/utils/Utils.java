/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Locale;
import java.util.Random;
import net.minecraft.Util;

/**
 * This is a "high-quality misc" class. Helper methods that are commonly used throughout the project
 * without an aspect on minecraft can be put here. Keep the names short, but distinct.
 */
public final class Utils {
    private static final Locale gameLocale = Locale.ROOT;
    private static final Random random = new Random();

    public static Locale getGameLocale() {
        return gameLocale;
    }

    public static Random getRandom() {
        return random;
    }

    /**
     * Open the specified URL in the user's browser if possible, otherwise copy it to the clipboard
     * and send it to chat.
     * @param url The url to open
     */
    public static void openUrl(String url) {
        Util.getPlatform().openUri(url);
    }

    public static void copyToClipboard(String s) {
        if (s == null) {
            clearClipboard();
        } else {
            copyToClipboard(new StringSelection(s));
        }
    }

    public static void copyToClipboard(StringSelection s) {
        if (s == null) {
            clearClipboard();
        } else {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
        }
    }

    public static void clearClipboard() {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new Transferable() {
                            public DataFlavor[] getTransferDataFlavors() {
                                return new DataFlavor[0];
                            }

                            public boolean isDataFlavorSupported(DataFlavor flavor) {
                                return false;
                            }

                            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                                throw new UnsupportedFlavorException(flavor);
                            }
                        },
                        null);
    }
}
