/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.event.NotificationEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = "Overlays")
public class GameNotificationOverlayFeature extends UserFeature {
    private static GameNotificationOverlayFeature INSTANCE;
    private static final List<MessageContainer> messageQueue = new LinkedList<>();

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final GameNotificationOverlay gameNotificationOverlay = new GameNotificationOverlay();

    @SubscribeEvent
    public void onGameNotification(NotificationEvent.Queue event) {
        messageQueue.add(event.getMessageContainer());

        if (GameNotificationOverlayFeature.INSTANCE.gameNotificationOverlay.overrideNewMessages
                && messageQueue.size() > GameNotificationOverlayFeature.INSTANCE.gameNotificationOverlay.messageLimit) {
            messageQueue.remove(0);
        }
    }

    @SubscribeEvent
    public void onGameNotification(NotificationEvent.Edit event) {
        MessageContainer newContainer = event.getMessageContainer();
        messageQueue.stream()
                .filter(messageContainer -> messageContainer.hashCode() == newContainer.hashCode())
                .findFirst()
                .ifPresent(messageContainer -> {
                    messageContainer.update(newContainer);
                });
    }

    public static class GameNotificationOverlay extends Overlay {

        @Config
        public int messageLimit = 5;

        @Config
        public boolean invertGrowth = true;

        @Config
        public int messageMaxLength = 0;

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean overrideNewMessages = true;

        private TextRenderSetting textRenderSetting;

        public GameNotificationOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    new GuiScaledOverlaySize(250, 110));

            updateTextRenderSetting();
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            List<MessageContainer> toRender = new ArrayList<>();

            ListIterator<MessageContainer> messages = messageQueue.listIterator(messageQueue.size());
            while (messages.hasPrevious()) {
                MessageContainer message = messages.previous();

                if (message.getRemainingTime() <= 0.0f) {
                    messages.remove(); // remove the message if the time has come
                    continue;
                }

                TextRenderTask messageTask = message.getRenderTask();

                if (messageMaxLength == 0 || messageTask.getText().length() < messageMaxLength) {
                    toRender.add(message);
                } else {
                    MessageContainer first = new MessageContainer(
                            messageTask.getText().substring(0, messageMaxLength), message.getEndTime());
                    MessageContainer second = new MessageContainer(
                            messageTask.getText().substring(messageMaxLength), message.getEndTime());
                    if (this.invertGrowth) {
                        toRender.add(first);
                        toRender.add(second);
                    } else {
                        toRender.add(second);
                        toRender.add(first);
                    }
                }
            }

            if (toRender.isEmpty()) return;

            List<MessageContainer> renderedValues = this.overrideNewMessages
                    ? toRender.subList(0, Math.min(toRender.size(), this.messageLimit))
                    : toRender.subList(Math.max(toRender.size() - this.messageLimit, 0), toRender.size());

            Collections.reverse(renderedValues);

            if (this.invertGrowth) {
                while (renderedValues.size() < messageLimit) {
                    renderedValues.add(0, new MessageContainer(""));
                }
            }

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            renderedValues.stream()
                                    .map(messageContainer -> messageContainer
                                            .getRenderTask()
                                            .setSetting(textRenderSetting.withCustomColor(messageContainer
                                                    .getRenderTask()
                                                    .getSetting()
                                                    .customColor()
                                                    .withAlpha(messageContainer.getRemainingTime() / 1000f))))
                                    .toList(),
                            this.getRenderedWidth(),
                            this.getRenderedHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSetting();
        }

        private void updateTextRenderSetting() {
            textRenderSetting = TextRenderSetting.getWithHorizontalAlignment(
                            this.getWidth(), CommonColors.WHITE, this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow);
        }
    }

    public static GameNotificationOverlayFeature getInstance() {
        return INSTANCE;
    }
}
