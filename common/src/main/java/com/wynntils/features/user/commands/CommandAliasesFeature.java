/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.commands;

import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.TypeOverride;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.CommandSentEvent;
import com.wynntils.mc.event.CommandsPacketEvent;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMMANDS)
public class CommandAliasesFeature extends UserFeature {
    @Config(visible = false)
    private List<CommandAlias> aliases = new ArrayList<>(List.of(
            new CommandAlias("guild attack", List.of("gu a", "guild a")),
            new CommandAlias("guild manage", List.of("gu m", "gu man", "guild m", "guild man")),
            new CommandAlias("guild territory", List.of("gu t", "gu terr", "guild t", "guild terr"))));

    @TypeOverride
    private final Type aliasesType = new TypeToken<List<CommandAlias>>() {}.getType();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCommandSent(CommandSentEvent e) {
        String message = e.getCommand();

        for (CommandAlias commandAlias : aliases) {
            if (commandAlias.getAliases().stream().anyMatch(alias -> Objects.equals(alias, message))) {
                e.setCanceled(true);
                McUtils.sendCommand(commandAlias.getOriginalCommand());
                break;
            }
        }
    }

    @SubscribeEvent
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode root = event.getRoot();

        for (CommandAlias commandAlias : aliases) {
            for (String alias : commandAlias.getAliases()) {
                String[] parts = alias.split(" ");
                LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(parts[0]);

                for (int i = 1; i < parts.length; i++) {
                    builder.then(Commands.literal(parts[i]));
                }

                root.addChild(builder.build());
            }
        }
    }

    private static final class CommandAlias {
        private final String originalCommand;
        private final List<String> aliases;

        private CommandAlias(String originalCommand, List<String> aliases) {
            this.originalCommand = originalCommand;
            this.aliases = aliases;
        }

        private List<String> getAliases() {
            return aliases;
        }

        private String getOriginalCommand() {
            return originalCommand;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            CommandAlias that = (CommandAlias) other;
            return originalCommand.equals(that.originalCommand) && aliases.equals(that.aliases);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalCommand, aliases);
        }
    }
}
