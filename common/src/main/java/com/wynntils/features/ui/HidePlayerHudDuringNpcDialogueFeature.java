/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.mc.event.FirstPersonHandRenderEvent;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import java.util.regex.Pattern;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class HidePlayerHudDuringNpcDialogueFeature extends Feature {
    private static final long DIALOGUE_MESSAGE_TIMEOUT_MS = 5000L;
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");

    private boolean dialogueProtectionActive = false;
    private long lastDialogueMessageTime = 0L;

    public HidePlayerHudDuringNpcDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderPlayerHud(RenderEvent.Pre event) {
        if (!isDialogueActive()) return;
        if (event.getType() != RenderElementType.CROSSHAIR && event.getType() != RenderElementType.HOTBAR) return;

        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFirstPersonHandRender(FirstPersonHandRenderEvent event) {
        if (!isDialogueActive()) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onChatReceived(SystemMessageEvent.ChatReceivedEvent event) {
        updateDialogueMessageTime(event);
    }

    @SubscribeEvent
    public void onGameInfoReceived(SystemMessageEvent.GameInfoReceivedEvent event) {
        updateDialogueMessageTime(event);
    }

    private void updateDialogueMessageTime(SystemMessageEvent event) {
        StyledText message = event.getStyledText();
        String formattedMessage = message.getString(StyleType.DEFAULT);
        String[] lines = formattedMessage.split("\\R");

        for (String line : lines) {
            StyledText styledLine = StyledText.fromString(line);
            if (styledLine.matches(NPC_CONFIRM_PATTERN) || styledLine.matches(NPC_SELECT_PATTERN)) {
                lastDialogueMessageTime = System.currentTimeMillis();
                return;
            }
        }
    }

    @SubscribeEvent
    public void onStatusEffectUpdate(MobEffectEvent.Update event) {
        if (event.getEntity() != McUtils.player()) return;

        if (event.getEffect().equals(MobEffects.SLOWNESS.value())
                && event.getEffectAmplifier() == 3
                && event.getEffectDurationTicks() == 32767) {
            dialogueProtectionActive = true;
        }
    }

    @SubscribeEvent
    public void onStatusEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEntity() != McUtils.player()) return;
        if (!event.getEffect().equals(MobEffects.SLOWNESS.value())) return;

        dialogueProtectionActive = false;
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) return;

        dialogueProtectionActive = false;
        lastDialogueMessageTime = 0L;
    }

    private boolean isDialogueActive() {
        return dialogueProtectionActive
                || System.currentTimeMillis() - lastDialogueMessageTime <= DIALOGUE_MESSAGE_TIMEOUT_MS;
    }
}
