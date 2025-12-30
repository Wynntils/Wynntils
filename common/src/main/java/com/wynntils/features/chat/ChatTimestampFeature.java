/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.AddGuiMessageLineEvent;
import com.wynntils.mc.event.ChatComponentRenderEvent;
import com.wynntils.mc.extension.GuiMessageExtension;
import com.wynntils.mc.extension.GuiMessageLineExtension;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class ChatTimestampFeature extends Feature {
    @Persisted
    private final Config<String> formatPattern = new Config<>("HH:mm:ss");

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);
    private int timestampWidth = 0;

    public ChatTimestampFeature() {
        super(ProfileDefault.DISABLED);
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        // Try to set the new format string and if it fails revert to the default
        try {
            formatter = DateTimeFormatter.ofPattern(formatPattern.get(), Locale.ROOT);
        } catch (Exception e) {
            formatter = null;

            McUtils.sendErrorToClient(I18n.get("feature.wynntils.chatTimestamp.invalidFormatMsg"));
        }
    }

    // We only format the timestamp when adding the line to improve performance
    @SubscribeEvent
    public void onGuiMessageLineAdd(AddGuiMessageLineEvent event) {
        GuiMessageExtension messageExtension = (GuiMessageExtension) (Object) event.getMessage();
        ((GuiMessageLineExtension) (Object) event.getLine()).setCreated(messageExtension.getCreated());

        if (event.getIndex() != 0 || formatter == null) return;

        GuiMessageLineExtension extension = (GuiMessageLineExtension) (Object) event.getLine();

        extension.setTimestamp(Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(extension.getCreated().format(formatter))
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY)));
    }

    @SubscribeEvent
    public void onChatComponentRenderPre(ChatComponentRenderEvent.Pre event) {
        timestampWidth = 0;

        for (GuiMessage.Line line : event.getChatComponent().trimmedMessages) {
            GuiMessageLineExtension extension = (GuiMessageLineExtension) (Object) line;
            Optional<Pair<Component, Integer>> timestamp = extension.getTimestamp();

            if (timestamp.isEmpty()) continue;

            timestampWidth = Math.max(timestampWidth, timestamp.get().b());
        }
    }

    @SubscribeEvent
    public void onChatComponentTranslate(ChatComponentRenderEvent.Translate event) {
        if (timestampWidth != 0) {
            // Moves the vanilla chatbox to the right
            event.setX(event.getX() + 4 + timestampWidth);
        }
    }

    @SubscribeEvent
    public void onChatComponentMapMouseX(ChatComponentRenderEvent.MapMouseX event) {
        if (timestampWidth != 0) {
            // Account for the translation so that hover/click events work properly
            event.setX(event.getX() - (4 + timestampWidth));
        }
    }

    @SubscribeEvent
    public void onChatComponentRenderBackground(ChatComponentRenderEvent.Background event) {
        if (timestampWidth == 0) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate((float) -(timestampWidth + 4), 0f, 0f);

        event.getGuiGraphics()
                .fill(
                        -2,
                        event.getRenderX() - event.getLineHeight(),
                        timestampWidth - 2,
                        event.getRenderX(),
                        event.getOpacity() << 24);

        event.getGuiGraphics().pose().popPose();
    }

    @SubscribeEvent
    public void onnChatComponentRenderText(ChatComponentRenderEvent.Text event) {
        if (timestampWidth == 0) return;

        GuiMessageLineExtension extension = (GuiMessageLineExtension) (Object) event.getLine();

        if (extension.getTimestamp().isEmpty()) return;

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(-(extension.getTimestamp().get().b() + 4f), 0f, 0f);

        event.getGuiGraphics()
                .drawString(
                        event.getFont(),
                        extension.getTimestamp().get().a(),
                        0,
                        event.getRenderY(),
                        16777215 + (event.getTextOpacity() << 24));

        event.getGuiGraphics().pose().popPose();
    }
}
