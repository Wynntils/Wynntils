/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.BarOverlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.OverlaySize;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.token.event.TokenGatekeeperEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class TokenTrackerFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> playSound = new Config<>(true);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final TokenBarOverlay tokenBarOverlay = new TokenBarOverlay();

    @SubscribeEvent
    public void onInventoryUpdated(TokenGatekeeperEvent.InventoryUpdated event) {
        if (!playSound.get()) return;

        // Do not play sound when depositing from the inventory
        if (event.getCount() < event.getOldCount()) return;

        if (Models.Token.getCollected(event.getGatekeeper()).isAtCap()) {
            McUtils.mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.7f, 0.75f));
        }
    }

    private static final class TokenBarOverlay extends BarOverlay {
        @RegisterConfig
        public final Config<ChatFormatting> color = new Config<>(ChatFormatting.GOLD);

        private TokenBarOverlay() {
            super(
                    new OverlayPosition(
                            120,
                            -5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.TopRight),
                    new OverlaySize(81, 21));
        }

        @Override
        public BarOverlayTemplatePair getTemplate() {
            return new BarOverlayTemplatePair(color.get().toString() + "{token_type}: {tokens}", "tokens");
        }

        @Override
        public BarOverlayTemplatePair getPreviewTemplate() {
            return new BarOverlayTemplatePair(color.get().toString() + "Tokens: 3/10", "capped(3; 10)");
        }

        @Override
        public boolean isRendered() {
            return Models.Token.getGatekeepers().findFirst().isPresent();
        }

        @Override
        public CustomColor getRenderColor() {
            return CustomColor.fromChatFormatting(color.get());
        }

        @Override
        public Texture getTexture() {
            return Texture.UNIVERSAL_BAR;
        }

        @Override
        protected float getTextureHeight() {
            return Texture.UNIVERSAL_BAR.height() / 2f;
        }
    }
}
