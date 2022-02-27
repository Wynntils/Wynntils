/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.core.commands.ClientCommands;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {

    @Shadow @Final EditBox input;

    @Redirect(
            method = "updateCommandInfo",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Suggestions> redirectSuggestions(
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher,
            ParseResults<SharedSuggestionProvider> serverParse,
            int cursor) {
        String command = input.getValue();

        CommandDispatcher<CommandSourceStack> clientDispatcher =
                ClientCommands.getClientSideCommands();
        ParseResults<CommandSourceStack> clientParse =
                clientDispatcher.parse(command, ClientCommands.getSource());

        CompletableFuture<Suggestions> clientSuggestions =
                clientDispatcher.getCompletionSuggestions(clientParse);
        CompletableFuture<Suggestions> serverSuggestions =
                serverDispatcher.getCompletionSuggestions(serverParse);

        CompletableFuture<Suggestions> result = new CompletableFuture<>();

        CompletableFuture.allOf(clientSuggestions, serverSuggestions)
                .thenRun(
                        () -> {
                            final List<Suggestions> suggestions = new ArrayList<>();
                            suggestions.add(clientSuggestions.join());
                            suggestions.add(serverSuggestions.join());
                            result.complete(Suggestions.merge(command, suggestions));
                        });

        return result;
    }
}
