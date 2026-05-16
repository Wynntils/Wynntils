/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.models.characterstats.actionbar.segments.DialogueSegment;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.DialogueUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class NpcDialogueFeature extends Feature {
    @Persisted
    private final Config<Boolean> tryKeepColors = new Config<>(true);

    @Persisted
    private final Config<DialogueHudOptions> dialogueHudOptions = new Config<>(DialogueHudOptions.KEEP_UNMODIFIED);

    @Persisted
    private final Config<Boolean> renderOverChat = new Config<>(true);

    @Persisted
    private final Config<Boolean> sendToChat = new Config<>(false);

    @Persisted
    private final Config<ColorChatFormatting> chatColor = new Config<>(ColorChatFormatting.GREEN);

    public NpcDialogueFeature() {
        super(ProfileDefault.ENABLED);
    }

    private static final int minimumEqualMessages = 6;

    private String lastText;
    private volatile String lastDispatched;
    private volatile Component lastComponent;
    private volatile Component lastModifiedComp;
    private int eqCount = 0;

    @SubscribeEvent
    public void onChatRenderPost(RenderEvent.Post event) {
        Component tempDialogue = Models.NpcDialogue.renderDialogue;

        if (Models.NpcDialogue.renderOverChat && tempDialogue != null) {
            /**
             * This mirrors the dialogue overlay rendering logic from Minecraft's
             * {@link net.minecraft.client.gui.Gui#renderOverlayMessage(GuiGraphics, DeltaTracker)}.
             */
            Profiler.get().push("dialogueOverlay");

            Font font = McUtils.mc().font;
            GuiGraphics guiGraphics = event.getGuiGraphics();

            guiGraphics.nextStratum();
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float) (guiGraphics.guiWidth() / 2), (float) (guiGraphics.guiHeight() - 68));
            int j = ARGB.white(255);

            int k = font.width(tempDialogue);
            guiGraphics.drawStringWithBackdrop(font, tempDialogue, -k / 2, -4, k, j);
            guiGraphics.pose().popMatrix();

            Profiler.get().pop();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onActionBarUpdate(SystemMessageEvent.GameInfoReceivedEvent event) {
        if (event.isCanceled()) return;

        manageDialogueHud(event);

        if (dialogueHudOptions.get() == DialogueHudOptions.HIDE) {
            event.setMessage(DialogueUtils.hideDialogueHud(event.getMessage()));
        }
    }

    private void manageDialogueHud(SystemMessageEvent.GameInfoReceivedEvent event) {
        DialogueUtils.Content content = DialogueUtils.getDialogueContent(event.getMessage(), tryKeepColors.get());
        String currentText = content.getText();

        if (currentText == null || currentText.isBlank()) {
            lastComponent = event.getMessage();
            return;
        }

        boolean sameText = currentText.equals(lastText);
        lastText = currentText;
        eqCount = sameText ? eqCount + 1 : 0;

        // Only start translating, when the message writing is done and not changing anymore.
        // 6 packets with the same text content = message is complete
        if (eqCount < minimumEqualMessages) return;

        // Get tanslated Text from Cache or Null if isn't present.
        String translatedText = Models.NpcDialogue.getFromCache(currentText);

        // Only send translation request if text isn't found in small cache
        if (translatedText == null) {
            lastModifiedComp = null;
            lastComponent = event.getMessage();

            if (eqCount == minimumEqualMessages) {
                // start translation (only once)
                startTranslationAndDispatch(currentText, content);
            }
            return;
        }

        // dispatch only once to chat and other listeners
        if (!translatedText.equals(lastDispatched)) {
            lastDispatched = translatedText;
            Models.NpcDialogue.dispatchContent(content, translatedText, sendToChat.get());
        }

        if (currentText.equals(translatedText)) return; // translator disabled
        if (dialogueHudOptions.get() != DialogueHudOptions.REPLACE_ORIGINAL) return;

        Component currentComponent = event.getMessage();

        // If it's the same component as before, we can reuse the cached one. (Performance)
        if (lastModifiedComp != null && currentComponent.equals(lastComponent)) {
            event.setMessage(lastModifiedComp);
            return;
        }

        // Modify the component and cache it
        lastComponent = currentComponent;
        lastModifiedComp = DialogueUtils.insertDialogueText(currentComponent, translatedText, content.getStartPos());
        event.setMessage(lastModifiedComp);
    }

    private void startTranslationAndDispatch(String message, DialogueUtils.Content content) {
        String capturedLastDispatched = lastDispatched;
        Models.NpcDialogue.requestDialogueTranslation(message, (result) -> {
            // run after translation is done
            if (capturedLastDispatched == null || capturedLastDispatched.equals(lastDispatched)) {
                lastDispatched = result;
            }
            if (!result.equals(capturedLastDispatched)) {
                Models.NpcDialogue.dispatchContent(content, result, sendToChat.get());
            }
            Models.NpcDialogue.addCache(message, result);
        });
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "renderOverChat" ->
                Models.NpcDialogue.renderOverChat = ((Config<Boolean>) config).get() && this.isEnabled();
            case "chatColor" -> Models.NpcDialogue.chatColor = ((Config<ColorChatFormatting>) config).get();
        }
        eqCount = 0;
        Models.NpcDialogue.clearCache();
    }

    @Override
    public void onDisable() {
        // Just the be sure it is false.
        // Even if the render-events only get called when feature is enabled!
        Models.NpcDialogue.renderOverChat = false;
        Models.NpcDialogue.clearCache();
    }

    private enum DialogueHudOptions {
        KEEP_UNMODIFIED,
        REPLACE_ORIGINAL,
        HIDE
    }
}
