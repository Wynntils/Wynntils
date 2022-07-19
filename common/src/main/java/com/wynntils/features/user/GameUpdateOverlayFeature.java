/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.objects.MessageContainer;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@FeatureInfo(category = "overlays")
public class GameUpdateOverlayFeature extends UserFeature {
    private static GameUpdateOverlayFeature INSTANCE;

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final GameUpdateOverlay GameUpdateOverlay = new GameUpdateOverlay();

    private static final List<MessageContainer> messageQueue = new LinkedList<>();

    public static MessageContainer queueMessage(Component message) {
        if (!WynnUtils.onWorld()) return null;

        WynntilsMod.info("Message Queued: " + message);
        MessageContainer msgContainer = new MessageContainer(message);
        McUtils.mc().doRunTask(() -> {
            messageQueue.add(msgContainer);

            if (GameUpdateOverlayFeature.INSTANCE.GameUpdateOverlay.overrideNewMessages
                    && messageQueue.size() > GameUpdateOverlayFeature.INSTANCE.GameUpdateOverlay.messageLimit)
                messageQueue.remove(0);
        });
        return msgContainer;
    }

    public static void editMessage(MessageContainer msgContainer, Component newMessage) {
        msgContainer.editMessage(newMessage);
        msgContainer.resetRemainingTime();
    }

    public static void resetMessages() {
        McUtils.mc().doRunTask(messageQueue::clear);
    }

    public static class GameUpdateOverlay extends Overlay {

        @Config
        public int messageLimit = 5;

        @Config
        public float messageTimeLimit = 10f;

        @Config
        public boolean invertGrowth = true;

        @Config
        public int messageMaxLength = 0;

        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.OUTLINE;

        @Config
        public boolean overrideNewMessages = true;

        private TextRenderSetting textRenderSetting = TextRenderSetting.getWithHorizontalAlignment(
                this.getWidth(), CommonColors.GREEN, this.getRenderHorizontalAlignment());

        public GameUpdateOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Right,
                            OverlayPosition.AnchorSection.BottomRight),
                    500,
                    220);
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.WHITE,
                    this.getRenderX(),
                    this.getRenderY(),
                    this.getRenderX() + this.getWidth(),
                    this.getRenderY() + this.getHeight(),
                    0,
                    2);

            List<MessageContainer> toRender = new ArrayList<>();

            Iterator<MessageContainer> messages = messageQueue.iterator();
            while (messages.hasNext() && toRender.size() < messageLimit) {
                MessageContainer message = messages.next();

                if (message.getRemainingTime() <= 0.0f) {
                    messages.remove(); // remove the message if the time has come
                    continue;
                }

                toRender.add(message);
            }

            if (this.invertGrowth) {
                while (toRender.size() < messageLimit) {
                    toRender.add(new MessageContainer(new TextComponent("")));
                }
                Collections.reverse(toRender);
            }

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            toRender.stream()
                                    .map(messageContainer -> new TextRenderTask(
                                            messageContainer.getMessage().getContents(), textRenderSetting))
                                    .toList(),
                            this.getRenderedWidth(),
                            this.getRenderedHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            textRenderSetting = TextRenderSetting.getWithHorizontalAlignment(
                    this.getWidth(), CommonColors.GREEN, this.getRenderHorizontalAlignment());
        }
    }

    public static GameUpdateOverlayFeature getInstance() {
        return INSTANCE;
    }
}
