/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextShadow;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.WynntilsQuestBookScreen;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.RenderedStringUtils;
import com.wynntils.utils.CommonColors;
import com.wynntils.utils.CustomColor;
import com.wynntils.utils.Pair;
import com.wynntils.wynn.model.quests.QuestInfo;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class QuestButton extends WynntilsButton {
    private static final Pair<CustomColor, CustomColor> BUTTON_COLOR =
            Pair.of(new CustomColor(181, 174, 151), new CustomColor(121, 116, 101));
    private static final Pair<CustomColor, CustomColor> TRACKED_BUTTON_COLOR =
            Pair.of(new CustomColor(176, 197, 148), new CustomColor(126, 211, 106));
    private static final Pair<CustomColor, CustomColor> TRACKING_REQUESTED_BUTTON_COLOR =
            Pair.of(new CustomColor(255, 206, 127), new CustomColor(255, 196, 50));
    private final QuestInfo questInfo;
    private final WynntilsQuestBookScreen questBookScreen;

    public QuestButton(int x, int y, int width, int height, QuestInfo questInfo, WynntilsQuestBookScreen screen) {
        super(x, y, width, height, Component.literal("Quest Button"));
        this.questInfo = questInfo;
        this.questBookScreen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = getBackgroundColor();
        RenderUtils.drawRect(poseStack, backgroundColor, this.getX(), this.getY(), 0, this.width, this.height);

        int maxTextWidth = this.width - 10 - 11;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        RenderedStringUtils.getMaxFittingText(
                                questInfo.getName(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.getX() + 14,
                        this.getY() + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        TextShadow.NONE);

        Texture stateTexture =
                switch (questInfo.getStatus()) {
                    case STARTED -> Texture.QUEST_STARTED;
                    case COMPLETED -> Texture.QUEST_FINISHED;
                    case CAN_START -> Texture.QUEST_CAN_START;
                    case CANNOT_START -> Texture.QUEST_CANNOT_START;
                };

        RenderUtils.drawTexturedRect(
                poseStack,
                stateTexture.resource(),
                this.getX() + 1,
                this.getY() + 1,
                stateTexture.width(),
                stateTexture.height(),
                stateTexture.width(),
                stateTexture.height());
    }

    private CustomColor getBackgroundColor() {
        Pair<CustomColor, CustomColor> colors;

        if (this.questInfo.equals(Managers.Quest.getTrackedQuest())) {
            colors = TRACKED_BUTTON_COLOR;
        } else if (this.questInfo.equals(questBookScreen.getTrackingRequested())) {
            colors = TRACKING_REQUESTED_BUTTON_COLOR;
        } else {
            colors = BUTTON_COLOR;
        }

        return (this.isHovered ? colors.b() : colors.a());
    }

    // Not called
    @Override
    public void onPress() {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            trackQuest();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            Optional<Location> nextLocation = this.questInfo.getNextLocation();

            nextLocation.ifPresent(
                    location -> McUtils.mc().setScreen(MainMapScreen.create((float) location.x, (float) location.z)));
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            openQuestWiki();
        }

        return true;
    }

    private void trackQuest() {
        if (this.questInfo.isTrackable()) {
            McUtils.playSound(SoundEvents.ANVIL_LAND);
            if (this.questInfo.equals(Managers.Quest.getTrackedQuest())) {
                Managers.Quest.stopTracking();
                questBookScreen.setTrackingRequested(null);
            } else {
                Managers.Quest.startTracking(this.questInfo);
                questBookScreen.setTrackingRequested(this.questInfo);
            }
        }
    }

    private void openQuestWiki() {
        McUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
        Managers.Quest.openQuestOnWiki(questInfo);
    }

    public QuestInfo getQuestInfo() {
        return questInfo;
    }
}
