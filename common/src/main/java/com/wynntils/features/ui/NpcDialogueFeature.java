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
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.wynn.DialogueUtils;
import net.minecraft.network.chat.Component;
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
    private final Config<Boolean> sendToChat = new Config<>(true);

    @Persisted
    private final Config<ColorChatFormatting> chatColor = new Config<>(ColorChatFormatting.GREEN);

    // TODO: add Button for downloading extra fonts resource pack

    public NpcDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    private String lastText;
    private volatile String lastDispatched;
    private volatile Component lastComponent;
    private volatile Component lastModifiedComp;
    private int eqCount = 0;

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
        if (eqCount < 6) return;

        // Get tanslated Text from Cache or Null if isn't present.
        String translatedText = Models.NpcDialogue.getFromCache(currentText);

        // Only send translation request if text isn't found in small cache
        if (translatedText == null) {
            lastModifiedComp = null;
            lastComponent = event.getMessage();

            if (eqCount == 6) {
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
            Models.NpcDialogue.dispatchContent(content, result, sendToChat.get());
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
        Models.NpcDialogue.clearCache();
    }

    private enum DialogueHudOptions {
        KEEP_UNMODIFIED,
        REPLACE_ORIGINAL,
        HIDE
    }
}
