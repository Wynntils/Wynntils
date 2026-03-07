/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.List;
import java.util.Locale;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.COMMANDS)
public class CommandAliasesFeature extends Feature {
    @Persisted
    private final HiddenConfig<List<RootAlias>> rootAliases = new HiddenConfig<>(List.of(
            new RootAlias("partyfinder", List.of("pf")),
            new RootAlias("renameitem", List.of("ri")),
            new RootAlias("onlinemembers", List.of("om"))));

    @Persisted
    private final HiddenConfig<List<ArgumentAlias>> argumentAliases = new HiddenConfig<>(List.of(
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "attack", List.of("a", "atk")),
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "manage", List.of("m", "man")),
            new ArgumentAlias(List.of("guild", "gu", "guilds"), "territory", List.of("t", "terr")),
            new ArgumentAlias(List.of("party", "pa"), "create", List.of("c")),
            new ArgumentAlias(List.of("party", "pa"), "finder", List.of("f")),
            new ArgumentAlias(List.of("party", "pa"), "invite", List.of("i", "inv")),
            new ArgumentAlias(List.of("party", "pa"), "join", List.of("j")),
            new ArgumentAlias(List.of("party", "pa"), "kick", List.of("k")),
            new ArgumentAlias(List.of("player", "pl"), "guild", List.of("g")),
            new ArgumentAlias(List.of("player", "pl"), "lastseen", List.of("ls"))));

    // Running `/party kick` or any of its aliases caused heavy log-spam, hopefully this fixes it
    private boolean rewritingCommand = false;

    public CommandAliasesFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommandSent(CommandSentEvent e) {
        if (rewritingCommand) return;

        String message = e.getCommand();
        if (message.isEmpty()) return;

        String[] parts = message.split(" ");

        boolean changed = false;

        for (RootAlias rootAlias : rootAliases.get()) {
            if (rootAlias.aliases().contains(parts[0].toLowerCase(Locale.ROOT))) {
                parts[0] = rootAlias.original();
                changed = true;
                break;
            }
        }

        if (parts.length > 1) {
            for (ArgumentAlias argumentAlias : argumentAliases.get()) {
                if (!argumentAlias.roots().contains(parts[0].toLowerCase(Locale.ROOT))) continue;

                for (int i = 1; i < parts.length; i++) {
                    if (argumentAlias.aliases().contains(parts[i].toLowerCase(Locale.ROOT))) {
                        parts[i] = argumentAlias.original();
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            e.setCanceled(true);
            rewritingCommand = true;
            Handlers.Command.sendCommandImmediately(String.join(" ", parts));
            rewritingCommand = false;
        }
    }

    @SubscribeEvent
    public void onCommandSuggestions(CommandSuggestionEvent.Modify event) {
        String input = event.getInput();

        if (!event.getInput().toLowerCase(Locale.ROOT).contains(" ")) {
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
                if (currentArg.isEmpty() || alias.startsWith(currentArg.toLowerCase(Locale.ROOT))) {
                    event.addSuggestion(alias);
                }
            }
        }
    }

    private record RootAlias(String original, List<String> aliases) {}

    private record ArgumentAlias(List<String> roots, String original, List<String> aliases) {}
}
