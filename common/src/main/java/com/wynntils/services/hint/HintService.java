/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hint;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.services.hint.type.HintAction;
import com.wynntils.utils.mc.McUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class HintService extends Service {
    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final String UNBOUND_KEY = "key.keyboard.unknown";
    private static final Random RANDOM = new Random();

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{(?<action>[^:}]+):(?<value>[^}]+)\\}");

    private List<Map<String, String>> hints = new ArrayList<>();

    public HintService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_HINTS).handleReader(this::handleHintMessages);
    }

    private void handleHintMessages(Reader reader) {
        Type type = new TypeToken<List<Map<String, String>>>() {}.getType();
        hints = WynntilsMod.GSON.fromJson(reader, type);
    }

    public void sendHint() {
        String languageCode = McUtils.mc().getLanguageManager().getSelected();

        if (hints.isEmpty()) return;

        int hintNum = RANDOM.nextInt(hints.size());
        Map<String, String> currentHint = hints.get(hintNum);
        String hint;

        if (currentHint.containsKey(languageCode)) {
            hint = currentHint.get(languageCode);
        } else {
            hint = currentHint.getOrDefault(DEFAULT_LANGUAGE, "");
        }

        if (hint.isEmpty()) {
            WynntilsMod.warn("Hint " + hintNum + " has no " + DEFAULT_LANGUAGE + " translation.");
            return;
        }

        MutableComponent component = formatHint(hint);

        if (component.equals(Component.empty())) return;

        McUtils.sendMessageToClientWithPillHeader(component);
    }

    private MutableComponent formatHint(String hint) {
        StyledText styledText = StyledText.EMPTY;

        Matcher matcher = VARIABLE_PATTERN.matcher(hint);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                styledText = styledText.append(
                        StyledText.fromComponent(Component.literal(hint.substring(lastEnd, matcher.start()))));
            }

            String actionStr = matcher.group("action");
            HintAction hintAction = HintAction.fromString(actionStr);
            if (hintAction == null) {
                WynntilsMod.warn("Unknown hint action " + actionStr);
                return Component.empty();
            }

            String value = matcher.group("value");
            Component actionComponent =
                    switch (hintAction) {
                        case KEYBIND -> createKeybindPart(value);
                        case TOGGLE_COMMAND, WYNNTILS_COMMAND ->
                            createCommandPart(
                                    hintAction.name().toLowerCase(Locale.ROOT).replace("_command", ""), value);
                    };

            if (actionComponent.equals(Component.empty())) {
                return Component.empty();
            }

            styledText = styledText.append(StyledText.fromComponent(actionComponent));
            lastEnd = matcher.end();
        }

        if (lastEnd < hint.length()) {
            styledText = styledText.append(StyledText.fromComponent(Component.literal(hint.substring(lastEnd))));
        }

        return styledText.getComponent().withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    private MutableComponent createKeybindPart(String keybindName) {
        for (KeyMapping keyMapping : McUtils.options().keyMappings) {
            if (keyMapping.getName().equals(keybindName)) {
                String translated = keyMapping.getTranslatedKeyMessage().getString();
                if (translated.equals(I18n.get(UNBOUND_KEY))) {
                    WynntilsMod.info("Skipping hint due to unbound key");
                    return Component.empty();
                }

                return Component.literal(translated).withStyle(ChatFormatting.YELLOW);
            }
        }

        WynntilsMod.info("Skipping hint due to unknown keybind " + keybindName);
        return Component.empty();
    }

    private MutableComponent createCommandPart(String command, String argument) {
        String fullCommand = "/" + command + " " + argument;
        return Component.literal(fullCommand).withStyle(style -> style.withColor(ChatFormatting.GOLD)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCommand))
                .withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to run " + fullCommand))));
    }
}
