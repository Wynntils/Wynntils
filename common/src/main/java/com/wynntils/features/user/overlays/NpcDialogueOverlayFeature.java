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
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.event.NpcDialogEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(category = FeatureCategory.OVERLAYS)
public class NpcDialogueOverlayFeature extends UserFeature {
    private static final Pattern NEW_QUEST_STARTED = Pattern.compile("^§r§6§lNew Quest Started: §r§e§l(.*)§r$");

    private final ScheduledExecutorService autoProgressExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledAutoProgressKeyPress = null;

    private String currentDialogue;

    @Config
    public boolean autoProgress = false;

    @Config
    public int dialogAutoProgressDefaultTime = 1600; // Milliseconds

    @Config
    public int dialogAutoProgressAdditionalTimePerWord = 300; // Milliseconds

    @RegisterKeyBind
    public final KeyBind cancelAutoProgressKeybind =
            new KeyBind("Cancel Dialog Auto Progress", GLFW.GLFW_KEY_Y, false, this::cancelAutoProgress);

    private void cancelAutoProgress() {
        if (scheduledAutoProgressKeyPress == null) return;

        scheduledAutoProgressKeyPress.cancel(true);
    }

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(Models.Chat.getClass());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent e) {
        String msg = e.getChatMessage() == null ? null : ComponentUtils.getCoded(e.getChatMessage());
        if (msg != null && NEW_QUEST_STARTED.matcher(msg).find()) {
            // TODO: Show nice banner notification instead
            // but then we'd also need to confirm it with a sneak
            NotificationManager.queueMessage(msg);
        }
        currentDialogue = msg;

        if (scheduledAutoProgressKeyPress != null) {
            scheduledAutoProgressKeyPress.cancel(true);

            // Release sneak key if currently pressed
            McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                    McUtils.player(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));

            scheduledAutoProgressKeyPress = null;
        }

        if (autoProgress) {
            // Schedule a new sneak key press if this is not the end of the dialogue
            if (msg != null) {
                scheduledAutoProgressKeyPress = scheduledSneakPress(msg);
            }
        }
    }

    private ScheduledFuture<?> scheduledSneakPress(String msg) {
        int words = msg.split(" ").length;
        long delay = dialogAutoProgressDefaultTime + ((long) words * dialogAutoProgressAdditionalTimePerWord);

        return autoProgressExecutor.schedule(
                () -> McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                        McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY)),
                delay,
                TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        currentDialogue = null;
        cancelAutoProgress();
    }

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay npcDialogueOverlay = new NpcDialogueOverlay();

    public class NpcDialogueOverlay extends Overlay {
        @Config
        public FontRenderer.TextShadow textShadow = FontRenderer.TextShadow.NORMAL;

        @Config
        public float backgroundOpacity = 0.2f;

        @Config
        public boolean stripColors = false;

        @Config
        public boolean showHelperTexts = true;

        private TextRenderSetting renderSetting;

        protected NpcDialogueOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.BottomMiddle),
                    new GuiScaledOverlaySize(400, 50),
                    HorizontalAlignment.Center,
                    VerticalAlignment.Middle);
            updateTextRenderSettings();
        }

        private void updateTextRenderSettings() {
            renderSetting = TextRenderSetting.DEFAULT
                    .withMaxWidth(this.getWidth() - 5)
                    .withHorizontalAlignment(this.getRenderHorizontalAlignment())
                    .withTextShadow(textShadow);
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
            updateDialogExtractionSettings();
            updateTextRenderSettings();
        }

        private void updateDialogExtractionSettings() {
            if (isEnabled()) {
                Models.Chat.addNpcDialogExtractionDependent(NpcDialogueOverlayFeature.this);
            } else {
                Models.Chat.removeNpcDialogExtractionDependent(NpcDialogueOverlayFeature.this);
                currentDialogue = null;
            }
        }

        private void renderDialogue(PoseStack poseStack, String currentDialogue) {
            TextRenderTask dialogueRenderTask = new TextRenderTask(currentDialogue, renderSetting);

            if (stripColors) {
                dialogueRenderTask.setText(ComponentUtils.stripColorFormatting(dialogueRenderTask.getText()));
            }

            float textHeight = FontRenderer.getInstance()
                    .calculateRenderHeight(
                            dialogueRenderTask.getText(),
                            dialogueRenderTask.getSetting().maxWidth());

            // Draw a translucent background
            float rectHeight = textHeight + 10;
            float rectRenderY =
                    switch (this.getRenderVerticalAlignment()) {
                        case Top -> this.getRenderY();
                        case Middle -> this.getRenderY() + (this.getHeight() - rectHeight) / 2f;
                        case Bottom -> this.getRenderY() + this.getHeight() - rectHeight;
                    };
            int colorAlphaRect = Math.round(MathUtils.clamp(255 * backgroundOpacity, 0, 255));
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.BLACK.withAlpha(colorAlphaRect),
                    this.getRenderX(),
                    rectRenderY,
                    0,
                    this.getWidth(),
                    rectHeight);

            // Render the message
            FontRenderer.getInstance()
                    .renderTextWithAlignment(
                            poseStack,
                            this.getRenderX(),
                            this.getRenderY(),
                            dialogueRenderTask,
                            this.getWidth(),
                            this.getHeight(),
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment());

            if (showHelperTexts) {
                // Render "To continue" message
                List<TextRenderTask> renderTaskList = new LinkedList<>();
                TextRenderTask pressSneakMessage = new TextRenderTask("§cPress SNEAK to continue", renderSetting);
                renderTaskList.add(pressSneakMessage);

                if (scheduledAutoProgressKeyPress != null && !scheduledAutoProgressKeyPress.isCancelled()) {
                    long timeUntilProgress = scheduledAutoProgressKeyPress.getDelay(TimeUnit.MILLISECONDS);
                    TextRenderTask autoProgressMessage = new TextRenderTask(
                            ChatFormatting.GREEN + "Auto-progress: "
                                    + Math.max(0, Math.round(timeUntilProgress / 1000f))
                                    + " seconds (Press "
                                    + ComponentUtils.getUnformatted(cancelAutoProgressKeybind
                                            .getKeyMapping()
                                            .getTranslatedKeyMessage())
                                    + " to cancel)",
                            renderSetting);
                    renderTaskList.add(autoProgressMessage);
                }

                FontRenderer.getInstance()
                        .renderTextsWithAlignment(
                                poseStack,
                                this.getRenderX() + 5,
                                this.getRenderY() + 20 + textHeight,
                                renderTaskList,
                                this.getWidth() - 30,
                                this.getHeight() - 30,
                                this.getRenderHorizontalAlignment(),
                                this.getRenderVerticalAlignment());
            }
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
