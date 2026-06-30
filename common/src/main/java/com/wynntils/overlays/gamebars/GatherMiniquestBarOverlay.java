/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.combat.bossbar.DamageBar;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;

public class GatherMiniquestBarOverlay extends BaseBarOverlay {
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
            ".*Bring §3\\[(\\d+) ([\\w\\s]+)\\]§r or §3\\[(\\d+) ([\\w\\s]+)\\]§r to the Gathering Post at §b.*");
    private boolean everythingMatched = false;

    private int itemAmount = 0;
    private String item1Name = "";
    private String item2Name = "";

    private CappedValue progress = CappedValue.EMPTY;

    public GatherMiniquestBarOverlay() {
        super(
                new OverlayPosition(
                        10,
                        -10,
                        VerticalAlignment.MIDDLE,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_LEFT),
                new OverlaySize(100, 50),
                ActivityType.MINI_QUEST.getColor());
    }

    @Override
    protected boolean isVisible() {
        return everythingMatched;
    }

    @Override
    protected BossBarProgress progress() {
        int itemsInInventory =
                Models.Inventory.getAmountInInventory(item1Name) + Models.Inventory.getAmountInInventory(item2Name);
        progress = new CappedValue(itemsInInventory, itemAmount);
        return new BossBarProgress(progress, (float) progress.getProgress());
    }

    @Override
    protected void renderText(GuiGraphics guiGraphics, float renderY, String text) {
        String formattedText = "";
        if (progress.getProgress() >= 1f) {
            formattedText += "§a";
        }
        formattedText += "Gathering: %s / %s".formatted(progress.current(), progress.max());
        super.renderText(guiGraphics, renderY, formattedText);
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return DamageBar.class;
    }

    @SubscribeEvent
    public void OnActivityUpdate(ActivityTrackerUpdatedEvent event) {
        String activityName = event.getName();
        StyledText activityTask = event.getTask();
        ActivityType activityType = event.getType();

        if (activityType == null || activityTask == null || activityName == null) {
            everythingMatched = false;
            return;
        }
        if (activityType != ActivityType.MINI_QUEST && !activityName.startsWith("Gather ")) {
            everythingMatched = false;
            return;
        }

        Matcher descriptionMatcher = activityTask.getMatcher(DESCRIPTION_PATTERN);

        if (!descriptionMatcher.matches()) {
            everythingMatched = false;
            return;
        }

        try {
            itemAmount = Integer.parseInt(descriptionMatcher.group(1));
            item1Name = descriptionMatcher.group(2);
            item2Name = descriptionMatcher.group(4);
        } catch (NumberFormatException e) {
            return;
        }

        everythingMatched = true;
    }
}
