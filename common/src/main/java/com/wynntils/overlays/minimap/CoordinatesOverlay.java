/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minimap;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.wynnfonts.WynntilsCoordinatesFont;
import com.wynntils.features.map.MinimapFeature;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CardinalDirection;
import com.wynntils.utils.wynn.LocationUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;

public class CoordinatesOverlay extends Overlay {
    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    private final Config<CompassDirectionYPos> compassDirectionYPos = new Config<>(CompassDirectionYPos.DIRECTION);

    @Persisted
    private final Config<CompassStyle> compassStyle = new Config<>(CompassStyle.STATIC);

    @Persisted
    private final Config<CompassEnd> compassEnd = new Config<>(CompassEnd.TAIL);

    @Persisted
    protected final Config<Float> fontScale = new Config<>(1.0f);

    private BlockPos lastBlockPos = BlockPos.ZERO;
    private CardinalDirection lastCardinalDirection = CardinalDirection.SOUTH;
    private StyledText coordinatesText = StyledText.EMPTY;

    public CoordinatesOverlay() {
        super(
                new OverlayPosition(
                        6,
                        -2,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(130, 20),
                HorizontalAlignment.RIGHT,
                VerticalAlignment.TOP);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        event.setRenderCoordinates(this.shouldDisplayOriginal.get());
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        coordinatesText,
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
    protected boolean isVisible() {
        if (Managers.Feature.getFeatureInstance(MinimapFeature.class)
                        .minimapOverlay
                        .hideWhenUnmapped
                        .get()
                == MinimapOverlay.UnmappedOption.MINIMAP_AND_COORDS) {
            return Services.Map.isPlayerInMappedArea(130, 130, 1);
        }

        return true;
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        coordinatesText = StyledText.fromComponent(WynntilsCoordinatesFont.buildCoordinates(
                compassStyle.get(), compassDirectionYPos.get(), compassEnd.get(), lastCardinalDirection, lastBlockPos));
    }

    @Override
    public void tick() {
        BlockPos currentPos = McUtils.player().blockPosition();
        CardinalDirection cardinalDirection = LocationUtils.getCardinalDirection(
                McUtils.mc().gameRenderer.getMainCamera().yRot());

        if (currentPos.equals(lastBlockPos) && cardinalDirection == lastCardinalDirection) return;

        lastBlockPos = currentPos;
        lastCardinalDirection = cardinalDirection;

        coordinatesText = StyledText.fromComponent(WynntilsCoordinatesFont.buildCoordinates(
                compassStyle.get(), compassDirectionYPos.get(), compassEnd.get(), lastCardinalDirection, lastBlockPos));
    }

    public enum CompassDirectionYPos {
        DIRECTION,
        Y_POS,
        BOTH
    }

    public enum CompassStyle {
        STATIC,
        ANIMATED,
        NONE
    }

    public enum CompassEnd {
        HEAD,
        TAIL
    }
}
