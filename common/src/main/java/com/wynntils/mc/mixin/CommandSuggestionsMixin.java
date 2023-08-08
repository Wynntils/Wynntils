/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.CommandSuggestionsEvent;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @WrapOperation(
            method = "updateCommandInfo()V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;",
                            remap = false))
    private CompletableFuture<Suggestions> onCompletionSuggestions(
            CommandDispatcher<SharedSuggestionProvider> serverDispatcher,
            ParseResults<SharedSuggestionProvider> serverParse,
            int cursor,
            Operation<CompletableFuture<Suggestions>> original,
            @Local StringReader stringReader) {
        CompletableFuture<Suggestions> serverSuggestions = original.call(serverDispatcher, serverParse, cursor);

        CommandSuggestionsEvent event = new CommandSuggestionsEvent(serverSuggestions, stringReader, cursor);
        MixinHelper.post(event);
        return event.getSuggestions();
    }
}
