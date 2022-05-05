/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.InventoryRenderEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FeatureInfo(stability = Stability.INVARIABLE, gameplay = GameplayImpact.LARGE, performance = PerformanceImpact.MEDIUM)
public class ItemScreenshotFeature extends Feature {

    private static Pattern ITEM_PATTERN = Pattern.compile("(Normal|Set|Unique|Rare|Legendary|Fabled|Mythic) Item.*");
    Slot hoveredSlot = null;

    private final KeyHolder itemScreenshotKeybind = new KeyHolder("Screenshot Item", GLFW.GLFW_KEY_F4, "Wynntils", true, this::takeScreenshot);

    private void takeScreenshot() {
        if (!WynnUtils.onWorld()) return;
        Screen screen = McUtils.mc().screen;
        if (!(screen instanceof AbstractContainerScreen<?>)) return;

        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;
        ItemStack stack = hoveredSlot.getItem();
        List<Component> tooltip = stack.getTooltipLines(McUtils.player(), TooltipFlag.Default.NORMAL);
        removeItemLore(tooltip);

        Font f = McUtils.mc().font;
        int width = 0;
        int height = 16;

        // width calculation
        for (Component c : tooltip) {
            int w = f.width(c.getString());
            if (w > width) {
                width = w;
            }
        }
        width += 8;

        // height calculation
        if (tooltip.size() > 1) {
            height += 2 + (tooltip.size() - 1) * 10;
        }

        // account for text wrapping
        if (width > screen.width/2 + 8) {
            int wrappedWidth = 0;
            int wrappedLines = 0;
            for (Component c : tooltip) {
                List<String> wrappedLine = listFormattedStringToWidth(c.toString(), screen.width/2);
                for (String ws : wrappedLine) {
                    wrappedLines++;
                    int w = f.width(ws);
                    if (w > wrappedWidth) {
                        wrappedWidth = w;
                    }
                }
            }
            width = wrappedWidth + 8;
            height = 16 + (2 + (wrappedLines - 1) * 10);
        }

        // calculate tooltip size to fit to framebuffer
        float scaleh = (float) screen.height/height;
        float scalew = (float) screen.width/width;

        // draw tooltip to framebuffer, create image
        // TODO: figure this out
        GlStateManager.

        // copy to clipboard
        ClipboardImage ci = new ClipboardImage(bi);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ci, null);
    }

    private void removeItemLore(List<Component> tooltip) {
        List<Component> toRemove = new ArrayList<>();
        boolean lore = false;
        for (Component c : tooltip) {
            // only remove text after the item type indicator
            Matcher m = ITEM_PATTERN.matcher(c.getString());
            if (!lore && m.find()) {
                lore = true;
            }

            if (lore && c.getString().contains("§8")) toRemove.add(c);
        }
        tooltip.removeAll(toRemove);
    }

    private List<String> listFormattedStringToWidth(String str, int maxWidth) {
        Font f = McUtils.mc().font;
        String[] words = str.split(" ");
        int runningWidth = 0;
        StringBuilder sb = new StringBuilder();
        List<String> toReturn = new ArrayList<>();
        for (String w : words) {
            if (runningWidth + f.width(w) <= maxWidth) {
                sb.append(w);
                runningWidth += f.width(w);
            } else {
                toReturn.add(sb.toString());
                sb = new StringBuilder();
                sb.append(w);
                runningWidth = f.width(w);
            }
        }
        return toReturn;
    }

    private static class ClipboardImage implements Transferable {

        Image image;

        public ClipboardImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) throw new UnsupportedFlavorException(flavor);
            return this.image;
        }
    }

    @SubscribeEvent
    private void setHoveredSlot(InventoryRenderEvent e) {
        hoveredSlot = e.getHoveredSlot();
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.itemScreenshot.name");
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        KeyManager.registerKeybind(itemScreenshotKeybind);
        return true;
    }

    @Override
    protected void onDisable() {
        KeyManager.unregisterKeybind(itemScreenshotKeybind);
    }
}
