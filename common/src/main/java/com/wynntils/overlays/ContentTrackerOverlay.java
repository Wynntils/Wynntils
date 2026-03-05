/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.models.activities.ActivityTrackerScoreboardPart;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class ContentTrackerOverlay extends TextOverlay {
    @Persisted
    private final Config<Boolean> disableTrackerOnScoreboard = new Config<>(true);

    @Persisted
    private final Config<Style> displayStyle = new Config<>(Style.MODERN);

    public ContentTrackerOverlay() {
        super(
                new OverlayPosition(
                        5,
                        -5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.RIGHT,
                        OverlayPosition.AnchorSection.TOP_RIGHT),
                new OverlaySize(300, 50),
                HorizontalAlignment.LEFT,
                VerticalAlignment.MIDDLE);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
        if (disableTrackerOnScoreboard.get()
                && event.getSegment().getScoreboardPart() instanceof ActivityTrackerScoreboardPart) {
            event.setCanceled(true);
        }
    }

    @Override
    protected String getTemplate() {
        return displayStyle.get().template;
    }

    @Override
    protected String getPreviewTemplate() {
        return """
               {with_atlas_sprite_font(styled_text("A");"items";"wynn/gui/content_book/quest_active")}§r§#29cc96ffQuest — Moving Wynntils Overlays§r
               You can select any overlay from Overlay Manager and edit it freely, moving, resizing and changing its rendering order.
               You can also hover over it while editing to show a tooltip with keyboard shortcuts!
               """;
    }

    @Override
    protected boolean shouldTextFit() {
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        super.render(guiGraphics, deltaTracker, window);
    }

    @Override
    protected boolean isVisible() {
        return Models.Activity.isTracking();
    }

    private enum Style {
        MODERN(
                """
                {activity_icon}§rÀ{with_font(with_color(styled_text(concat(activity_type; " - "; activity_name));activity_color);"language/wynncraft")}§r
                {activity_task(true)}
                """),
        NO_ICON(
                """
                {with_font(with_color(styled_text(concat(activity_type; " - "; activity_name));activity_color);"language/wynncraft")}§r
                {activity_task(true)}
                """),
        LEGACY(
                """
                §aTracked {activity_type}:
                §6{activity_name}§r
                {activity_task(true)}
                """);

        public final String template;

        Style(String template) {
            this.template = template;
        }
    }
}
