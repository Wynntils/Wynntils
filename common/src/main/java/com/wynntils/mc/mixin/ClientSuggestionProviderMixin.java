/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {
    @Inject(
            method =
                    "customSuggestion(Lcom/mojang/brigadier/context/CommandContext;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("HEAD"),
            cancellable = true)
    private void onCustomSuggestion(
            CommandContext<?> context, CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        String input = context.getInput();

        // Ensure it is a command with no spaces, we only want to set suggestions for the root
        if (input.startsWith("/") && !input.contains(" ")) {
            SuggestionsBuilder builder = new SuggestionsBuilder(input, 1);

            CommandSuggestionEvent event = new CommandSuggestionEvent.Add(input.substring(1));
            MixinHelper.post(event);

            for (String root : event.getSuggestions()) {
                builder.suggest(root);
            }

            cir.setReturnValue(builder.buildFuture());
        }
    }
}
