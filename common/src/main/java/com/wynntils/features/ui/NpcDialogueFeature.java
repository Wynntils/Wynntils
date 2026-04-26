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
import com.wynntils.models.npcdialogue.event.OverlayDisplayEvent;
import com.wynntils.utils.colors.ColorChatFormatting;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.DialogueUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class NpcDialogueFeature extends Feature {
    @Persisted
    private final Config<Boolean> tryKeepColors = new Config<>(true);

    @Persisted
    private final Config<Boolean> replaceDialogueHud = new Config<>(true);

    @Persisted
    private final Config<Boolean> sendToChat = new Config<>(false);

    @Persisted
    private final Config<ColorChatFormatting> chatColor = new Config<>(ColorChatFormatting.GREEN);

    public NpcDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    /**
     * If you make a custom Overlay, turn this to true when it's enabled
     * to clear to original wynncraft hud
     * */
    private boolean isCustomOverlayEnabled() {
        return false;
    }

    private String lastText;
    private String lastDispatched;
    private volatile Component lastComponent;
    private volatile Component lastModifiedComp;
    private int eqCount = 0;

    @SubscribeEvent
    public void onOverlayReceived(OverlayDisplayEvent overlay) {
        if (overlay.isCanceled()) return;

        Models.NpcDialogue.lastDialogueContent = overlay.getContent();
        String currentText = overlay.getText();

        if (currentText == null || currentText.isBlank()) {
            Models.NpcDialogue.lastTranslatedText = null;
            lastComponent = overlay.getComponent();
            return;
        }

        boolean sameText = currentText.equals(lastText);
        lastText = currentText;
        eqCount = sameText ? eqCount + 1 : 0;

        // We get many packets per second.
        // Only start translating, when the message writing is done and not changing anymore.
        if (eqCount < 6) {
            Models.NpcDialogue.lastTranslatedText = null;
            return;
        }

        String translatedText = Models.NpcDialogue.getFromCache(currentText);
        Models.NpcDialogue.lastTranslatedText = translatedText;

        // Only send translation request if text isn't found in small cache
        if (translatedText == null) {
            lastModifiedComp = null;
            lastComponent = overlay.getComponent();

            if (eqCount == 6) {
                Models.NpcDialogue.requestDialogueTranslation(overlay.getContent());
            }
            return;
        }

        // send only once to chat and other listeners
        if (!translatedText.equals(lastDispatched)) {
            lastDispatched = translatedText;
            Models.NpcDialogue.dispatchContent(overlay.getContent(), translatedText, sendToChat.get());
        }

        if (currentText.equals(translatedText)) return;
        if (!replaceDialogueHud.get()) return;

        // Cancel the original overlay because we render our own modified version.
        overlay.setCanceled(true);
        Component currentComponent = overlay.getComponent();

        // if it's the same component as before, we can reuse the cached one. (Performance)
        if (lastModifiedComp != null && currentComponent.equals(lastComponent)) {
            Component cached = lastModifiedComp;
            McUtils.mc().execute(() -> showNewContent(cached));
            return;
        }

        lastModifiedComp = null;
        lastComponent = currentComponent;

        McUtils.mc().execute(() -> {
            if (isCustomOverlayEnabled()) {
                lastModifiedComp = DialogueUtils.insertDialogueText(
                        currentComponent, "", overlay.getContent().getStartPos());
            } else {
                lastModifiedComp = DialogueUtils.insertDialogueText(
                        currentComponent, translatedText, overlay.getContent().getStartPos());
            }
            showNewContent(lastModifiedComp);
        });
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "tryKeepColors" -> Models.NpcDialogue.keepColors = ((Config<Boolean>) config).get();
            case "chatColor" -> Models.NpcDialogue.chatColor = ((Config<ColorChatFormatting>) config).get();
        }
        eqCount = 0;
        Models.NpcDialogue.clearCache();
    }

    @Override
    public void onDisable() {
        Models.NpcDialogue.clearCache();
    }

    private void showNewContent(Component newContent) {
        McUtils.mc().getChatListener().handleSystemMessage(newContent, true);
    }
}
