/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.List;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMMANDS)
public class CommandAliasesFeature extends Feature {
    @Persisted
    private final HiddenConfig<List<RootAlias>> rootAliases =
            new HiddenConfig<>(List.of(new RootAlias("partyfinder", List.of("pf"))));

    @Persisted
    private final HiddenConfig<List<ArgumentAlias>> argumentAliases = new HiddenConfig<>(List.of(
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "attack", List.of("a")),
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "manage", List.of("m", "man")),
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "territory", List.of("t", "terr"))));

    public CommandAliasesFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommandSent(CommandSentEvent e) {
        String message = e.getCommand();
        if (message.isEmpty()) return;

        String[] parts = message.split(" ");

        boolean changed = false;

        for (RootAlias rootAlias : rootAliases.get()) {
            if (rootAlias.aliases().contains(parts[0])) {
                parts[0] = rootAlias.original();
                changed = true;
                break;
            }
        }

        if (parts.length > 1) {
            for (ArgumentAlias argumentAlias : argumentAliases.get()) {
                if (!argumentAlias.roots().contains(parts[0])) continue;

                for (int i = 1; i < parts.length; i++) {
                    if (argumentAlias.aliases().contains(parts[i])) {
                        parts[i] = argumentAlias.original();
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            e.setCanceled(true);
            Handlers.Command.sendCommandImmediately(String.join(" ", parts));
        }
    }

    @SubscribeEvent
    public void onCommandSuggestions(CommandSuggestionEvent.Modify event) {
        String input = event.getInput();

        if (!event.getInput().contains(" ")) {
            for (RootAlias rootAlias : rootAliases.get()) {
                for (String alias : rootAlias.aliases()) {
                    if (alias.startsWith(event.getInput())) {
                        event.addSuggestion(alias);
                    }
                }
            }

            return;
        }

        int lastSpace = input.lastIndexOf(' ');
        String root = input.substring(0, lastSpace);
        String currentArg = input.substring(lastSpace + 1);

        for (ArgumentAlias argumentAlias : argumentAliases.get()) {
            if (!argumentAlias.roots().contains(root)) continue;

            for (String alias : argumentAlias.aliases()) {
                if (currentArg.isEmpty() || alias.startsWith(currentArg)) {
                    event.addSuggestion(alias);
                }
            }
        }
    }

    private record RootAlias(String original, List<String> aliases) {}

    private record ArgumentAlias(List<String> roots, String original, List<String> aliases) {}
}
