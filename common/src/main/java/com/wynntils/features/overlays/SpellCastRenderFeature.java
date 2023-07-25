/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.event.ItemRenamedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class SpellCastRenderFeature extends Feature {
    private static final int SHOW_TICKS = 40;

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public Overlay spellCastOverlay = new SpellCastOverlay();

    @RegisterConfig
    public final Config<Boolean> renderVignette = new Config<>(true);

    @RegisterConfig
    public final Config<Integer> vignetteFadeTime = new Config<>(12);

    @RegisterConfig
    public final Config<Float> vignetteIntensity = new Config<>(0.75f);

    @RegisterConfig
    public final Config<CustomColor> vignetteColor = new Config<>(new CustomColor(0, 71, 201));

    private int spellTimer;
    private StyledText spellMessage;
    private float intensity;

    @SubscribeEvent
    public void onItemRename(ItemRenamedEvent event) {
        if (!InventoryUtils.isWeapon(event.getItemStack())) return;

        // Hide vanilla item rename popup
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onSpellCast(SpellEvent.Cast event) {
        int manaCost = event.getManaCost();
        spellMessage = StyledText.fromString(
                "§7" + event.getSpellType().getName() + " spell cast! §3[§b-" + manaCost + " ✺§3]");

        // An relativeCost of 1.0 means we just used all mana we have left
        float relativeCost = (float) manaCost / Models.CharacterStats.getMana().current();
        intensity = vignetteIntensity.get() * relativeCost;
        spellTimer = SHOW_TICKS;
    }

    @SubscribeEvent
    public void onSpellFailed(SpellEvent.Failed event) {
        spellMessage = event.getFailureReason().getMessage();
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
        if (!renderVignette.get() || intensity <= 0f) return;

        int shownTicks = SHOW_TICKS - spellTimer;
        int fade = vignetteFadeTime.get() - shownTicks;
        if (fade > 0) {
            float alpha = intensity * ((float) fade / vignetteFadeTime.get());
            RenderUtils.renderVignetteOverlay(event.getPoseStack(), vignetteColor.get(), alpha);
        }
    }

    public class SpellCastOverlay extends Overlay {
        protected SpellCastOverlay() {
            super(
                    new OverlayPosition(
                            -100,
                            0,
                            VerticalAlignment.BOTTOM,
                            HorizontalAlignment.CENTER,
                            OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                    new OverlaySize(200, 20),
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.BOTTOM);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
            if (spellTimer <= 0) return;

            // Render it the same way vanilla renders item changes
            int alpha = (int) Math.min((float) spellTimer * 256.0F / 10.0F, 255.0F);
            if (alpha <= 0) return;

            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            spellMessage,
                            this.getRenderX(),
                            this.getRenderX() + this.getWidth(),
                            this.getRenderY(),
                            this.getRenderY() + this.getHeight(),
                            this.getWidth(),
                            CommonColors.WHITE.withAlpha(alpha),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            TextShadow.NORMAL);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }
}
