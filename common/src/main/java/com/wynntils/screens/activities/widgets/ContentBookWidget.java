/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.activities.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.activities.type.ActivityTrackingState;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.screens.activities.ContentBookHolder;
import com.wynntils.screens.activities.WynntilsContentBookScreen;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ContentBookWidget extends AbstractWidget implements TooltipProvider {
    private static final CustomColor UNAVAILABLE_COLOR = CustomColor.fromHexString("#AD976C");

    private final ContentBookHolder holder;
    private final ActivityInfo activityInfo;
    private final ItemStack itemStack;
    private final Integer slot;
    private final List<Component> tooltip;
    private final boolean searchMatch;

    private Style nameStyle = Style.EMPTY;

    public ContentBookWidget(
            int x,
            int y,
            Pair<ItemStack, ActivityInfo> activityInfoPair,
            int slot,
            ContentBookHolder holder,
            boolean searchMatch) {
        super(x, y, 132, 16, Component.literal("Content Book Activity Button"));

        this.itemStack = activityInfoPair.a();
        this.activityInfo = activityInfoPair.b();
        this.slot = slot;
        this.holder = holder;
        this.searchMatch = searchMatch;

        List<Component> addons = new ArrayList<>();

        addons.add(Component.empty());

        if (canSetCompass()) {
            addons.add(Component.translatable("screens.wynntils.contentBook.leftClickToSetCompass")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.GREEN));
        }
        if (canOpenMap()) {
            addons.add(Component.translatable("screens.wynntils.contentBook.middleClickToOpenOnMap")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.YELLOW));
        }
        addons.add(Component.translatable("screens.wynntils.contentBook.rightClickToOpenWiki")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.GOLD));

        this.tooltip = LoreUtils.appendTooltip(itemStack, LoreUtils.getTooltipLines(itemStack), addons);

        if (activityInfo.trackingState() == ActivityTrackingState.TRACKED) {
            nameStyle = nameStyle.withBold(true).withUnderlined(true);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final CustomColor gradientColor = activityInfo.status() == ActivityStatus.UNAVAILABLE
                ? UNAVAILABLE_COLOR
                : activityInfo.type().getColor();
        RenderUtils.fillSidewaysGradient(
                guiGraphics.pose(),
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                0,
                gradientColor,
                gradientColor.withAlpha(0));
        guiGraphics.renderItem(itemStack, getX(), getY());

        nameStyle = nameStyle.withBold(this.isHovered || activityInfo.trackingState() == ActivityTrackingState.TRACKED);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(
                                Component.literal(activityInfo.name()).withStyle(nameStyle)),
                        getX() + 18,
                        getY() + 8,
                        width - 18,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (holder.inTutorial) {
            RenderUtils.drawRotatingBorderSegment(
                    guiGraphics.pose(), CommonColors.RED, getX(), getY(), getX() + width, getY() + height, 1, 2, 0.25f);
        }

        if (searchMatch) return;

        RenderUtils.drawRect(guiGraphics.pose(), CommonColors.BLACK.withAlpha(100), getX(), getY(), 1, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (canSetCompass()) {
                Models.Activity.placeCompassOnActivity(activityInfo);
            } else {
                holder.pressSlot(slot);

                if (activityInfo.trackingState() == ActivityTrackingState.TRACKED
                        && McUtils.screen() instanceof WynntilsContentBookScreen contentBookScreen) {
                    contentBookScreen.removeTrackedActivity();
                }
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && canOpenMap()) {
            Models.Activity.openMapOnActivity(activityInfo);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            // Shift right clicking world events is used to fast travel to them
            if (activityInfo.type() == ActivityType.WORLD_EVENT
                    && activityInfo.status() == ActivityStatus.AVAILABLE
                    && KeyboardUtils.isShiftDown()) {
                holder.pressSlot(slot, button);
            } else {
                Models.Activity.openActivityOnWiki(activityInfo);
            }
        }

        return true;
    }

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(tooltip);
    }

    private boolean canSetCompass() {
        return switch (activityInfo.type()) {
            case SECRET_DISCOVERY -> true;
            case WORLD_DISCOVERY, TERRITORIAL_DISCOVERY, BOSS_ALTAR ->
                activityInfo.status() != ActivityStatus.AVAILABLE
                        && activityInfo.trackingState() != ActivityTrackingState.TRACKED;
            default -> false;
        };
    }

    private boolean canOpenMap() {
        return switch (activityInfo.type()) {
            case QUEST, STORYLINE_QUEST ->
                activityInfo.status() == ActivityStatus.STARTED || activityInfo.status() == ActivityStatus.AVAILABLE;
            case MINI_QUEST ->
                activityInfo.status() != ActivityStatus.COMPLETED
                        && activityInfo.trackingState() != ActivityTrackingState.UNTRACKABLE;
            case WORLD_EVENT -> false;
            default -> true;
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
