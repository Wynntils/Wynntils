/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.characterstats.actionbar.segments.MeterTransitionSegment;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.RenderElementType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class MountJumpBarFeature extends Feature {
    private static final Pattern METER_CHARACTER_PATTERN = Pattern.compile(".+([\uE190-\uE19C]).+");
    private static final int BAR_HEIGHT = 6;

    private int padding = 0;

    public MountJumpBarFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onRenderPost(RenderEvent.Post event) {
        if (event.getType() != RenderElementType.ACTION_BAR) return;
        if (Models.Mount.getCurrentMountType().isEmpty()
                || Models.Mount.getCurrentMountType().get() == MountType.WYVERN) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int renderX = this.getRenderX(McUtils.window());
        int renderY = this.getRenderY(McUtils.window());

        float rawProgress = Mth.lerpDiscrete(McUtils.player().getJumpRidingScale(), 0, 182) / 182.0f;

        // The jump bar skips the middle part where it overlaps with the level number,
        // so it needs to be corrected in order to display an accurate jump scale.
        // One segment takes 0.428 of the xp bar, and the remaining 0.435 after skipping 0.565
        float progress;
        if (rawProgress < 0.5f) {
            progress = rawProgress / 0.856f;
        } else {
            progress = 0.565f + (0.435f * ((rawProgress - 0.5f) / 0.5f));
        }

        RenderUtils.drawProgressBar(
                guiGraphics,
                Texture.MOUNT_JUMP_BAR,
                renderX,
                renderY,
                renderX + Texture.MOUNT_JUMP_BAR.width(),
                renderY + BAR_HEIGHT / 2f,
                0,
                padding * BAR_HEIGHT,
                Texture.MOUNT_JUMP_BAR.width(),
                (BAR_HEIGHT - 1) + (padding * BAR_HEIGHT),
                progress);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(MeterTransitionSegment.class, this::updateMeterCharacter, this::clearMeterCharacter);
    }

    private int getRenderX(Window window) {
        return (window.getGuiScaledWidth() - 182) / 2 + 2;
    }

    private int getRenderY(Window window) {
        return window.getGuiScaledHeight() - 28;
    }

    private void updateMeterCharacter(MeterTransitionSegment segment) {
        Matcher matcher = METER_CHARACTER_PATTERN.matcher(segment.getSegmentText());
        if (matcher.matches()) {
            padding = switch (matcher.group(1)) {
                case "\uE191", "\uE199" -> 1;
                case "\uE192", "\uE198" -> 2;
                case "\uE193", "\uE194", "\uE195", "\uE196", "\uE197" -> 3;
                default -> 0;
            };
        }
    }

    private void clearMeterCharacter() {
        padding = 0;
    }
}
