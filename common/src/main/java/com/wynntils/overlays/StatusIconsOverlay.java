/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.models.statuseffects.type.StatusIconProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.SubscribeEvent;

public class StatusIconsOverlay extends Overlay {
    private List<RenderedStatusEffect> renderedStatusEffectsList = List.of();

    @SubscribeEvent
    public void onStatusChange(StatusEffectsChangedEvent event) {
        updateRenderedStatusEffectsList();
    }

    private void updateRenderedStatusEffectsList() {
        List<RenderedStatusEffect> updatedList = new ArrayList<>();
        List<StatusEffect> currentEffects = Models.StatusEffect.getStatusEffects();

        List<StatusEffect> filteredEffects = currentEffects.stream()
                .filter(statusEffect ->
                        (StatusIconProperty.fromString(statusEffect.getName().getString(PartStyle.StyleType.NONE))
                                != null))
                .toList();
        for (StatusEffect effect : filteredEffects) {
            RenderedStatusEffect oldEffect = renderedStatusEffectsList.stream()
                    .filter(e -> e.getName().equals(effect.getName()))
                    .findFirst()
                    .orElse(null);
            if (oldEffect != null) {
                oldEffect.progress = oldEffect.progress.withCurrent(effect.getDuration());
                updatedList.add(oldEffect);
                continue;
            }
            StatusIconProperty property =
                    StatusIconProperty.fromString(effect.getName().getString(PartStyle.StyleType.NONE));
            RenderedStatusEffect newEffect = new RenderedStatusEffect(
                    effect.getName(),
                    property.getTexture(),
                    property.getColor(),
                    new CappedValue(effect.getDuration(), effect.getDuration()));
            updatedList.add(newEffect);
        }
        renderedStatusEffectsList = updatedList;
    }

    public StatusIconsOverlay() {
        super(
                new OverlayPosition(
                        55,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(250, 110));

        updateRenderedStatusEffectsList();
    }

    @Override
    public void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        for (RenderedStatusEffect effectToRender : renderedStatusEffectsList) {
            effectToRender.render(
                    guiGraphics, getRenderX(), getRenderY(), getRenderX() + 18, getRenderY() + 18, 1f, 1f);
        }
    }

    private static final class RenderedStatusEffect {
        public StyledText name;
        public Texture texture;
        public CustomColor color;
        public CappedValue progress;

        private RenderedStatusEffect(StyledText name, Texture texture, CustomColor color, CappedValue progress) {
            this.name = name;
            this.texture = texture;
            this.color = color;
            this.progress = progress;
        }

        public void render(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float z, float lineWidth) {
            RenderUtils.drawTexturedRect(guiGraphics.pose(), texture, x1, y1);
            RenderUtils.drawRectBorderProgress(
                    guiGraphics.pose(), color, x1 + 1.5f, y1 + 1.5f, x2 - 1.5f, y2 - 1.5f, z, lineWidth, (float)
                            progress.getProgress());
        }

        public StyledText getName() {
            return name;
        }
    }
}
