/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.features.user.NpcDialogAutoProgressFeature;
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
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = "Overlays")
public class NpcDialogueOverlayFeature extends UserFeature {
    private static final Pattern NEW_QUEST_STARTED = Pattern.compile("^§6§lNew Quest Started: §r§e§l(.*)§r$");
    private String currentDialogue;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ChatModel.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent e) {
        String msg = e.getCodedDialog();
        if (msg != null && NEW_QUEST_STARTED.matcher(msg).find()) {
            // TODO: Show nice banner notification instead
            // but then we'd also need to confirm it with a sneak
            NotificationManager.queueMessage(msg);
        }
        currentDialogue = msg;
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
                    new GuiScaledOverlaySize(200, 150),
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
            updateDialogExtractionSettings();
            updateTextRenderSettings();
        }

        private void updateDialogExtractionSettings() {
            if (isEnabled()) {
                ChatModel.addNpcDialogExtractionDependent(NpcDialogueOverlayFeature.this);
            } else {
                ChatModel.removeNpcDialogExtractionDependent(NpcDialogueOverlayFeature.this);
            }
        }

        private void renderDialogue(PoseStack poseStack, String currentDialogue) {
            List<TextRenderTask> dialogueRenderTask = List.of(new TextRenderTask(currentDialogue, renderSetting));

            float textHeight = FontRenderer.getInstance().calculateRenderHeight(dialogueRenderTask);
            // Draw a translucent background
            int colorAlphaRect = 45;
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.BLACK.withAlpha(colorAlphaRect),
                    this.getRenderX(),
                    this.getRenderY(),
                    0,
                    this.getWidth(),
                    textHeight + 10);

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
            Optional<Long> millisecondsUntilProgress =
                    NpcDialogAutoProgressFeature.INSTANCE.millisecondsUntilProgress();

            List<TextRenderTask> renderTaskList;

            renderTaskList = millisecondsUntilProgress
                    .map(timeUntilProgress -> List.of(
                            new TextRenderTask("§cPress SNEAK to continue", renderSetting),
                            new TextRenderTask(
                                    ChatFormatting.GREEN + "Auto-progress: "
                                            + Math.max(0, Math.round(timeUntilProgress / 1000f))
                                            + " seconds (Press SPACE to cancel)",
                                    renderSetting)))
                    .orElseGet(() -> List.of(new TextRenderTask("§cPress SNEAK to continue", renderSetting)));

            FontRenderer.getInstance()
                    .renderTextsWithAlignment(
                            poseStack,
                            this.getRenderX() + 5,
                            this.getRenderY() + 20 + textHeight,
                            renderTaskList,
                            this.getRenderedWidth() - 15,
                            this.getRenderedHeight() - 15,
                            this.getRenderHorizontalAlignment(),
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
