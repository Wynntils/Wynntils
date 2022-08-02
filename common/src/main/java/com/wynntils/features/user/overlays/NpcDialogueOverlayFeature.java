/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.event.NpcDialogEvent;
import com.wynntils.wc.event.WorldStateEvent;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = "Overlays")
public class NpcDialogueOverlayFeature extends UserFeature {
    private static final Pattern NEW_QUEST_STARTED = Pattern.compile("^§6§lNew Quest Started: §r§e§l(.*)§r$");
    private String currentDialogue;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onNpcDialogue(NpcDialogEvent e) {
        String msg = e.getCodedDialog();
        if (msg != null && NEW_QUEST_STARTED.matcher(msg).find()) {
            // TODO: Show nice banner notification instead
            // but then we'd also need to confirm it with a sneak
            NotificationManager.queueMessage(msg);
        }
        currentDialogue = msg;
        // Cancel the event so the chat fallback does not get it
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        currentDialogue = null;
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay npcDialogueOverlay = new NpcDialogueOverlay();

    public class NpcDialogueOverlay extends Overlay {
        private TextRenderSetting renderSetting;

        public NpcDialogueOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.Middle),
                    new GuiScaledOverlaySize(200, 300),
                    HorizontalAlignment.Left,
                    VerticalAlignment.Top);
            updateTextRenderSettings();
        }

        private void updateTextRenderSettings() {
            renderSetting = TextRenderSetting.getWithHorizontalAlignment(
                    this.getWidth() - 5, CommonColors.WHITE, this.getRenderHorizontalAlignment());
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateTextRenderSettings();
        }

        private void renderDialogue(PoseStack poseStack, String currentDialogue) {
            List<TextRenderTask> dialogueRenderTask = List.of(new TextRenderTask(currentDialogue, renderSetting));

            // There's something fishy going on with calculateRenderHeight...
            float textHeight = FontRenderer.getInstance().calculateRenderHeight(dialogueRenderTask) / 2;
            // Draw a translucent background
            // TODO: Replace this with a more stylish background?
            RenderUtils.drawRectBorders(
                    poseStack,
                    CommonColors.DARK_GRAY,
                    this.getRenderX(),
                    this.getRenderY(),
                    this.getRenderX() + this.getWidth(),
                    this.getRenderY() + textHeight,
                    1,
                    1.8f);
            int colorAlphaRect = 45;
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.BLACK.withAlpha(colorAlphaRect),
                    this.getRenderX(),
                    this.getRenderY(),
                    0,
                    this.getWidth(),
                    textHeight);

            // Render the message
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX() + 5,
                            this.getRenderY() + 5,
                            dialogueRenderTask,
                            this.getRenderedWidth() - 10,
                            this.getRenderedHeight() - 10,
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());

            // Render "To continue" message
            // TODO: I'd like to have this better looking, perhaps a clickable button?
            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX() + 5,
                            this.getRenderY() + 10 + textHeight,
                            List.of(new TextRenderTask("§cPress SNEAK to continue", renderSetting)),
                            this.getRenderedWidth() - 15,
                            this.getRenderedHeight() - 15,
                            HorizontalAlignment.Left,
                            this.getRenderVerticalAlignment());
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            String currentDialogue = NpcDialogueOverlayFeature.this.currentDialogue;

            if (currentDialogue == null) return;

            renderDialogue(poseStack, currentDialogue);
        }

        @Override
        public void renderPreview(PoseStack poseStack, float partialTicks, Window window) {
            String fakeDialogue =
                    "§7[1/1] §r§2Random Citizen: §r§aDid you know that Wynntils is the best Wynncraft mod you'll probably find?§r";
            // we have to force update every time
            updateTextRenderSettings();

            renderDialogue(poseStack, fakeDialogue);
        }
    }
}
