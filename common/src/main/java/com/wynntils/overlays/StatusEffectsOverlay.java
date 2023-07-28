/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.google.common.collect.ComparisonChain;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.statuseffects.event.StatusEffectsChangedEvent;
import com.wynntils.models.statuseffects.type.StatusEffect;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class StatusEffectsOverlay extends Overlay {
    @RegisterConfig
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @RegisterConfig
    public final Config<Float> fontScale = new Config<>(1.0f);

    @RegisterConfig
    public final Config<Boolean> stackEffects = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> sortEffects = new Config<>(true);

    private List<TextRenderTask> renderCache = List.of();
    private TextRenderSetting textRenderSetting;

    public StatusEffectsOverlay() {
        super(
                new OverlayPosition(
                        55,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(250, 110));

        updateTextRenderSetting();
    }

    @SubscribeEvent
    public void onStatusChange(StatusEffectsChangedEvent event) {
        recalculateRenderCache();
    }

    private void recalculateRenderCache() {
        List<StatusEffect> effects = Models.StatusEffect.getStatusEffects();
        Stream<RenderedStatusEffect> effectWithProperties;

        if (stackEffects.get()) {
            effectWithProperties = stackEffects(effects);
        } else {
            effectWithProperties = effects.stream().map(RenderedStatusEffect::new);
        }

        if (sortEffects.get()) {
            // Sort effects based on their prefix and their name
            effectWithProperties = effectWithProperties.sorted(((t1, t2) -> t1.compare(t1.getEffect(), t2.getEffect())));
        }

        renderCache = effectWithProperties
                .map(statusTimer -> new TextRenderTask(statusTimer.getTextToRender(), getTextRenderSetting()))
                .toList();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        BufferedFontRenderer.getInstance()
                .renderTextsWithAlignment(
                        poseStack,
                        bufferSource,
                        this.getRenderX(),
                        this.getRenderY(),
                        renderCache,
                        this.getWidth(),
                        this.getHeight(),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        fontScale.get());
    }

    @Override
    public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
        BufferedFontRenderer.getInstance()
                .renderTextsWithAlignment(
                        poseStack,
                        bufferSource,
                        this.getRenderX(),
                        this.getRenderY(),
                        List.of(new TextRenderTask(
                                StyledText.fromString("§8⬤ §7 Purification 00:02"), textRenderSetting)),
                        this.getWidth(),
                        this.getHeight(),
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        fontScale.get());
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        updateTextRenderSetting();
        recalculateRenderCache();
    }

    private void updateTextRenderSetting() {
        textRenderSetting = TextRenderSetting.DEFAULT
                .withMaxWidth(this.getWidth())
                .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                .withTextShadow(textShadow.get());
    }

    protected TextRenderSetting getTextRenderSetting() {
        return textRenderSetting;
    }

    private Stream<RenderedStatusEffect> stackEffects(List<StatusEffect> effects) {
        Map<String, RenderedStatusEffect> effectsToRender = new LinkedHashMap<>();

        for (StatusEffect effect : effects) {
            RenderedStatusEffect entry =
                    effectsToRender.get(effect.asString().getString());
            if (entry == null) {
                entry = new RenderedStatusEffect(effect);
                effectsToRender.put(effect.asString().getString(), entry);
            }
            entry.count += 1;
        }
        return effectsToRender.values().stream();
    }

    private static final class RenderedStatusEffect implements Comparator<StatusEffect> {
        private int count = 0;
        private final StatusEffect effect;

        private RenderedStatusEffect(StyledText name, StyledText modifier, StyledText displayedTime, StyledText prefix) {
            this.effect = new StatusEffect(name, modifier, displayedTime, prefix);
        }

        private RenderedStatusEffect(StatusEffect effect) {
            this.effect = effect;
        }

        private StyledText getTextToRender() {
            return this.count > 1
                    ? StyledText.fromString(this.count + "x ").append(this.effect.asString())
                    : this.effect.asString();
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int c) {
            this.count = c;
        }

        public StatusEffect getEffect() {
            return this.effect;
        }

        @Override
        public int compare(StatusEffect effect, StatusEffect t1) {
            return ComparisonChain.start()
                    .compare(effect.getPrefix().getString(), t1.getPrefix().getString())
                    .compare(effect.getName().getString(), t1.getName().getString())
                    .compare(effect.getModifier().getString(), t1.getModifier().getString())
                    .result();
        }
    }
}
