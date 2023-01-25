/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpellCastRenderFeature extends UserFeature {
    private static final int SHOW_TICKS = 40;
    public static final int FADE_TICKS = 4;

    @Config
    public boolean renderVignette = true;

    @Config
    public float vignetteIntensity = 1.3f;

    @Config
    public CustomColor vignetteColor = new CustomColor(0, 0, 255);

    private int spellTimer;
    private Component spellMessage;
    private float intensity;

    @SubscribeEvent
    public void onItemRename(ItemRenamedEvent event) {
        ItemStack itemStack = event.getItemStack();
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isEmpty()) return;

        GearItem gearItem = gearItemOpt.get();
        if (!gearItem.getGearProfile().getGearInfo().getType().isWeapon()) return;

        // Hide vanilla item rename popup
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast event) {
        int manaCost = event.getManaCost();
        String msg = "§7" + event.getSpellType().getName() + " spell cast! §3[§b-" + manaCost + " ✺§3]";
        spellMessage = Component.literal(msg);

        // An relativeCost of 1.0 means we just used all mana we have left
        float relativeCost = (float) manaCost / Models.Character.getCurrentMana();
        intensity = vignetteIntensity * relativeCost;
        spellTimer = SHOW_TICKS;
    }

    @SubscribeEvent
    public void onSpellFailed(SpellEvent.Failed event) {
        spellMessage = Component.literal(event.getFailureReason().getMessage());
        intensity = 0f;
        spellTimer = SHOW_TICKS;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (spellTimer <= 0) return;

        spellTimer--;
    }

    @SubscribeEvent
    public void onRender(RenderEvent.Post event) {
        if (spellTimer <= 0) return;

        if (renderVignette && intensity > 0f) {
            int shownTicks = SHOW_TICKS - spellTimer;
            int fade = FADE_TICKS - shownTicks;
            if (fade > 0) {
                float alpha = intensity * ((float) fade / FADE_TICKS);
                RenderUtils.renderVignetteOverlay(event.getPoseStack(), vignetteColor, alpha);
            }
        }

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
