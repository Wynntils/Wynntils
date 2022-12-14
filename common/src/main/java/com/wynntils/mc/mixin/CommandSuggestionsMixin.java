/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.core.managers.Managers;
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
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Final
    EditBox input;

    private ParseResults<CommandSourceStack> clientParse;

    @Redirect(
            method = "updateCommandInfo",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;",
                            remap = false))
    private CompletableFuture<Suggestions> redirectSuggestions(
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher,
            ParseResults<SharedSuggestionProvider> serverParse,
            int cursor) {
        return Managers.CLIENT_COMMAND.getCompletionSuggestions(
                input.getValue(), serverDispatcher, clientParse, serverParse, cursor);
    }

    @Redirect(
            method = "updateCommandInfo",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;",
                            remap = false))
    private ParseResults<SharedSuggestionProvider> redirectParse(
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher, StringReader command, Object source) {
        CommandDispatcher<CommandSourceStack> clientDispatcher = Managers.CLIENT_COMMAND.getClientDispatcher();
        clientParse = clientDispatcher.parse(command, Managers.CLIENT_COMMAND.getSource());

        return serverDispatcher.parse(command, (SharedSuggestionProvider) source);
    }
}
