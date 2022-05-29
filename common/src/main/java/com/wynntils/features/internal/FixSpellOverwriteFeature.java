/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.wynntils.core.features.InternalFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.ClientTickEvent.Phase;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.mixin.accessors.GuiAccessor;
import com.wynntils.mc.utils.McUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
public class FixSpellOverwriteFeature extends InternalFeature {
    private static final Pattern SPELL_CAST = Pattern.compile("^§7(.*) spell cast!§3 \\[§b-([0-9]+) ✺§3\\]$");

    private int overwriteHighlightTimer;
    private ItemStack overwriteHighlightItem = ItemStack.EMPTY;

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) return;

        Matcher spellMatcher = SPELL_CAST.matcher(item.getHoverName().getString());
        if (!spellMatcher.find()) return;

        event.setCanceled(true);
        overwriteHighlightTimer = 40;
        overwriteHighlightItem = item;
    }

    @SubscribeEvent
    public void onTickEnd(ClientTickEvent event) {
        if (event.getTickPhase() != Phase.END) return;
        if (overwriteHighlightTimer <= 0 || overwriteHighlightItem.isEmpty()) return;

        Gui gui = McUtils.mc().gui;
        ((GuiAccessor) gui).setToolHighlightTimer(overwriteHighlightTimer);
        ((GuiAccessor) gui).setLastToolHighlight(overwriteHighlightItem);

        overwriteHighlightTimer--;
    }
}
