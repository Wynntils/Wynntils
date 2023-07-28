/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        Stream<StatusEffectWithProperties> effectWithProperties;

        if (stackEffects.get()) {
            effectWithProperties = stackEffects(effects);
        } else {
            effectWithProperties = effects.stream().map(StatusEffectWithProperties::new);
        }

        if (sortEffects.get()) {
            // Sort effects based on their prefix and their name
            effectWithProperties = effectWithProperties.sorted(Comparator.comparing(
                    e -> e.getPrefix().getString() + e.getName().getString()));
        }

        renderCache = effectWithProperties
                .map(statusTimer -> new TextRenderTask(getTextToRender(statusTimer), getTextRenderSetting()))
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

    private Stream<StatusEffectWithProperties> stackEffects(List<StatusEffect> effects) {
        Map<String, StatusEffectWithProperties> effectsToRender = new LinkedHashMap<>();

        for (StatusEffect effect : effects) {
            StatusEffectWithProperties entry =
                    effectsToRender.get(effect.asString().getString());
            if (entry == null) {
                entry = new StatusEffectWithProperties(effect);
                effectsToRender.put(effect.asString().getString(), entry);
            }
            entry.count += 1;
        }
        return effectsToRender.values().stream();
    }

    private StyledText getTextToRender(StatusEffectWithProperties effect) {
        return effect.count > 1
                ? StyledText.fromString(effect.count + "x ").append(effect.asString())
                : effect.asString();
    }

    private static final class StatusEffectWithProperties extends StatusEffect {
        public int count = 0;

        private StatusEffectWithProperties(
                StyledText name, StyledText modifier, StyledText displayedTime, StyledText prefix) {
            super(name, modifier, displayedTime, prefix);
        }

        private StatusEffectWithProperties(StatusEffect effect) {
            this(effect.getName(), effect.getModifier(), effect.getDisplayedTime(), effect.getPrefix());
        }
    }
}
