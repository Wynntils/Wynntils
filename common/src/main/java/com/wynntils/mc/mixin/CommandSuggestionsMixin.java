/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.mc.utils.managers.ClientCommandsManager;
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

    ParseResults<CommandSourceStack> clientParse;

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

        StringReader stringReader = new StringReader(input.getValue());
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }

        CommandDispatcher<CommandSourceStack> clientDispatcher =
                ClientCommandsManager.getClientDispatcher();

        CompletableFuture<Suggestions> clientSuggestions =
                clientDispatcher.getCompletionSuggestions(clientParse, cursor);
        CompletableFuture<Suggestions> serverSuggestions =
                serverDispatcher.getCompletionSuggestions(serverParse, cursor);

        CompletableFuture<Suggestions> result = new CompletableFuture<>();

        CompletableFuture.allOf(clientSuggestions, serverSuggestions)
                .thenRun(
                        () -> {
                            final List<Suggestions> suggestions = new ArrayList<>();
                            suggestions.add(clientSuggestions.join());
                            suggestions.add(serverSuggestions.join());
                            result.complete(
                                    Suggestions.merge(stringReader.getString(), suggestions));
                        });

        return result;
    }

    @Redirect(
            method = "updateCommandInfo",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;"))
    public ParseResults<SharedSuggestionProvider> redirectParse(
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher,
            StringReader command,
            Object source) {
        CommandDispatcher<CommandSourceStack> clientDispatcher =
                ClientCommandsManager.getClientDispatcher();
        clientParse = clientDispatcher.parse(command, ClientCommandsManager.getSource());

        return serverDispatcher.parse(command, (SharedSuggestionProvider) source);
    }
}
