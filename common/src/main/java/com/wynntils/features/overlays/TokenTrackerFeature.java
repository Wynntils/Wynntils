/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.overlays.BarOverlay;
import com.wynntils.core.consumers.features.overlays.ContainerOverlay;
import com.wynntils.core.consumers.features.overlays.OverlayPosition;
import com.wynntils.core.consumers.features.overlays.OverlaySize;
import com.wynntils.core.consumers.features.overlays.annotations.OverlayInfo;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.token.event.TokenGatekeeperEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.OVERLAYS)
public class TokenTrackerFeature extends Feature {
    @RegisterConfig
    public final Config<Boolean> playSound = new Config<>(true);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final TokenBarsOverlay tokenBarsOverlay = new TokenBarsOverlay(
            new OverlayPosition(
                    70, -5, VerticalAlignment.TOP, HorizontalAlignment.RIGHT, OverlayPosition.AnchorSection.TOP_RIGHT),
            new OverlaySize(81, 84),
            ContainerOverlay.GrowDirection.DOWN);

    @SubscribeEvent
    public void onInventoryUpdated(TokenGatekeeperEvent.InventoryUpdated event) {
        if (!playSound.get()) return;

        // Do not play sound when depositing from the inventory
        if (event.getCount() < event.getOldCount()) return;

        CappedValue deposited = event.getGatekeeper().getDeposited();
        int collected = Models.Token.inInventory(event.getGatekeeper()) + deposited.current();

        if (collected < deposited.max()) return;
        // Do not keep playing the sound if we go too far over the needed amount
        if (collected > deposited.max() + 5) return;

        McUtils.mc().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.7f, 0.75f));
    }

    @SubscribeEvent
    public void onGatekeeperAdded(TokenGatekeeperEvent.Added event) {
        tokenBarsOverlay.addChild(new TokenBarOverlay(tokenBarsOverlay.size() + 1));
    }

    @SubscribeEvent
    public void onGatekeeperRemoved(TokenGatekeeperEvent.Removed event) {
        tokenBarsOverlay.clearChildren();
        for (int i = 1; i <= Models.Token.getGatekeepers().size(); i++) {
            tokenBarsOverlay.addChild(new TokenBarOverlay(i));
        }
    }

    protected static final class TokenBarOverlay extends BarOverlay {
        @RegisterConfig
        public final Config<ColorChatFormatting> color = new Config<>(ColorChatFormatting.GOLD);

        private TokenBarOverlay(int id) {
            super(id, new OverlaySize(81, 21));
            horizontalAlignmentOverride.updateConfig(HorizontalAlignment.RIGHT);
            verticalAlignmentOverride.updateConfig(VerticalAlignment.TOP);
        }

        @Override
        public BarOverlayTemplatePair getTemplate() {
            return new BarOverlayTemplatePair(
                    color.get().getChatFormatting() + "{token_type(" + getId() + ")}: {token(" + getId() + ")}",
                    "token(" + getId() + ")");
        }

        @Override
        public BarOverlayTemplatePair getPreviewTemplate() {
            return new BarOverlayTemplatePair(color.get().getChatFormatting() + "Tokens: 3/10", "capped(3; 10)");
        }

        @Override
        public boolean isRendered() {
            return true;
        }

        @Override
        public CustomColor getRenderColor() {
            return CustomColor.fromChatFormatting(color.get().getChatFormatting());
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

    protected static class TokenBarsOverlay extends ContainerOverlay<TokenBarOverlay> {
        protected TokenBarsOverlay(OverlayPosition position, OverlaySize size, GrowDirection growDirection) {
            super(position, size, growDirection, HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
        }

        @Override
        protected List<TokenBarOverlay> getPreviewChildren() {
            return List.of(new TokenBarOverlay(0), new TokenBarOverlay(1));
        }
    }
}
