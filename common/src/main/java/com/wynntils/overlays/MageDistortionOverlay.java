/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.event.BossBarAddedEvent;
import com.wynntils.models.abilities.bossbars.DistortionBar;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public class MageDistortionOverlay extends Overlay {
    @Persisted
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted
    private final Config<Float> fontScale = new Config<>(1.0f);

    private MutableComponent component = Component.empty();

    private TextRenderSetting textRenderSetting;

    public MageDistortionOverlay() {
        super(
                new OverlayPosition(
                        150,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(80, 12));

        updateTextRenderSetting();
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.distortionBar.isActive();
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(component),
                        renderX,
                        renderX + this.getWidth(),
                        renderY,
                        renderY + this.getHeight(),
                        0,
                        CommonColors.WHITE,
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        TextShadow.NONE,
                        fontScale.get());
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(Component.empty()
                                .withColor(0xd599ff)
                                .append(Component.literal("\uE035")
                                        .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(
                                                Identifier.withDefaultNamespace("common")))))
                                .append(Component.literal(" Distortion: 50"))),
                        renderX,
                        renderX + this.getWidth(),
                        renderY,
                        renderY + this.getHeight(),
                        0,
                        CommonColors.WHITE,
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        TextShadow.NONE,
                        fontScale.get());
    }

    private void updateTextRenderSetting() {
        textRenderSetting = TextRenderSetting.DEFAULT
                .withMaxWidth(this.getWidth())
                .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                .withTextShadow(textShadow.get());
    }

    @Override
    public void tick() {
        component = Component.empty()
                .withColor(0xd599ff)
                .append(Component.literal("\uE035")
                        .withStyle(Style.EMPTY.withFont(
                                new FontDescription.Resource(Identifier.withDefaultNamespace("common")))))
                .append(Component.literal(" Distortion: ")
                        .append(String.valueOf(Models.Ability.distortionBar.getCurrent())));
    }

    @SubscribeEvent
    public void onBossBarAdd(BossBarAddedEvent event) {
        if (event.getTrackedBar().getClass().equals(DistortionBar.class)) event.setCanceled(true);
    }
}
