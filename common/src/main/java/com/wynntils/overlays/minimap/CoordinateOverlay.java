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
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public class CoordinateOverlay extends Overlay {
    private static final FontDescription WYNNTILS_COORDINATES_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("wynntils", "coordinates"));

    private static final String HEAD_CHARACTER = "\uE100";
    private static final String LARGE_GAP_CHARACTER = "\uE101";
    private static final String TAIL_CHARACTER = "\uE103";
    private static final String COMPASS_TAIL_CHARACTER = "\uE104";
    private static final String COMPASS_HEAD_CHARACTER = "\uE10C";
    private static final Component NEGATIVE_SPACE_CHARACTER = Component.literal("\uDAFF\uDFFF")
            .withStyle(Style.EMPTY.withFont(
                    new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "space"))));

    @Persisted
    private final Config<Boolean> shouldDisplayOriginal = new Config<>(false);

    @Persisted
    private final Config<Boolean> replaceDirection = new Config<>(false);

    @Persisted
    private final Config<CompassStyle> compassStyle = new Config<>(CompassStyle.STATIC);

    @Persisted
    private final Config<CompassEnd> compassEnd = new Config<>(CompassEnd.TAIL);

    @Persisted
    protected final Config<Float> fontScale = new Config<>(1.0f);

    private BlockPos lastBlockPos = BlockPos.ZERO;
    private CardinalDirection lastCardinalDirection = CardinalDirection.SOUTH;
    private StyledText coordinatesText = StyledText.EMPTY;

    public CoordinateOverlay() {
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
        buildCoordinates();
    }

    @Override
    public void tick() {
        BlockPos currentPos = McUtils.player().blockPosition();
        CardinalDirection cardinalDirection = LocationUtils.getCardinalDirection(
                McUtils.mc().gameRenderer.getMainCamera().yRot());

        if (currentPos.equals(lastBlockPos) && cardinalDirection == lastCardinalDirection) return;

        lastBlockPos = currentPos;
        lastCardinalDirection = cardinalDirection;

        buildCoordinates();
    }

    private void buildCoordinates() {
        MutableComponent component = Component.empty()
                .withStyle(Style.EMPTY.withFont(WYNNTILS_COORDINATES_FONT).withColor(ChatFormatting.WHITE));

        if (compassEnd.get() == CompassEnd.HEAD) {
            if (compassStyle.get() == CompassStyle.STATIC) {
                component.append(COMPASS_HEAD_CHARACTER);
            } else if (compassStyle.get() == CompassStyle.NONE) {
                component.append(HEAD_CHARACTER);
            } else if (compassStyle.get() == CompassStyle.ANIMATED) {
                char animated = (char) (COMPASS_HEAD_CHARACTER.charAt(0) + lastCardinalDirection.ordinal());
                component.append(Component.literal(String.valueOf(animated)));
            }
        } else {
            component.append(HEAD_CHARACTER);
        }

        component.append(NEGATIVE_SPACE_CHARACTER);
        component.append(LARGE_GAP_CHARACTER);
        component.append(NEGATIVE_SPACE_CHARACTER);

        String.valueOf(lastBlockPos.getX()).chars().forEach(c -> {
            component.append(Component.literal(String.valueOf((char) c)));
            component.append(NEGATIVE_SPACE_CHARACTER);
        });

        component.append(LARGE_GAP_CHARACTER);
        component.append(NEGATIVE_SPACE_CHARACTER);

        String middleChars =
                replaceDirection.get() ? String.valueOf(lastBlockPos.getY()) : lastCardinalDirection.getShortName();

        String.valueOf(middleChars).chars().forEach(c -> {
            component.append(Component.literal(String.valueOf((char) c)));
            component.append(NEGATIVE_SPACE_CHARACTER);
        });

        component.append(LARGE_GAP_CHARACTER);
        component.append(NEGATIVE_SPACE_CHARACTER);

        String.valueOf(lastBlockPos.getZ()).chars().forEach(c -> {
            component.append(Component.literal(String.valueOf((char) c)));
            component.append(NEGATIVE_SPACE_CHARACTER);
        });

        component.append(LARGE_GAP_CHARACTER);
        component.append(NEGATIVE_SPACE_CHARACTER);

        if (compassEnd.get() == CompassEnd.TAIL) {
            if (compassStyle.get() == CompassStyle.STATIC) {
                component.append(COMPASS_TAIL_CHARACTER);
            } else if (compassStyle.get() == CompassStyle.NONE) {
                component.append(TAIL_CHARACTER);
            } else if (compassStyle.get() == CompassStyle.ANIMATED) {
                char animated = (char) (COMPASS_TAIL_CHARACTER.charAt(0) + lastCardinalDirection.ordinal());
                component.append(Component.literal(String.valueOf(animated)));
            }
        } else {
            component.append(TAIL_CHARACTER);
        }

        coordinatesText = StyledText.fromComponent(component);
    }

    private enum CompassStyle {
        STATIC,
        ANIMATED,
        NONE
    }

    private enum CompassEnd {
        HEAD,
        TAIL
    }
}
