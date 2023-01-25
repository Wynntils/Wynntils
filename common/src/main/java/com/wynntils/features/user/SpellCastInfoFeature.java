/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellCastInfoFeature extends UserFeature {
    private static final int SHOW_TICKS = 40;

    private int spellTimer;
    private Component spellMessage;

    @SubscribeEvent
    public void onItemRename(ItemRenamedEvent event) {
        ItemStack itemStack = event.getItemStack();
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isEmpty()) return;

        GearItem gearItem = gearItemOpt.get();
        if (!gearItem.getGearProfile().getGearInfo().getType().isWeapon()) return;

        // Check that we are not just restoring the old name
        if (event.getNewName().equals(gearItem.getGearProfile().getDisplayName())) return;

        // This really is new info!
        event.setCanceled(true);
        spellTimer = SHOW_TICKS;
        spellMessage = Component.literal(event.getNewName());
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (spellTimer <= 0) return;

        spellTimer--;
    }

    @SubscribeEvent
    public void onRender(RenderEvent.Post event) {
        if (spellTimer <= 0) return;

        // Render it the same way vanilla renders item changes
        int alpha = (int) Math.min((float) spellTimer * 256.0F / 10.0F, 255.0F);
        if (alpha <= 0) return;

        Window window = McUtils.mc().getWindow();
        var screenWidth = window.getGuiScaledWidth();
        var screenHeight = window.getGuiScaledHeight();

        int width = McUtils.mc().gui.getFont().width(spellMessage);
        int x = (screenWidth - width) / 2;
        int y = screenHeight - 59;

        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        McUtils.mc().gui.getFont().drawShadow(poseStack, spellMessage, x, y, 0xFFFFFF + (alpha << 24));
        RenderSystem.disableBlend();
    }
}
