/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.wynntils.core.events.MixinHelper;
import com.wynntils.mc.event.CommandSuggestionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    @Final
    private EditBox input;

    @Inject(
            method = "updateCommandInfo()V",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/client/gui/components/CommandSuggestions;pendingSuggestions:Ljava/util/concurrent/CompletableFuture;",
                            opcode = Opcodes.PUTFIELD,
                            shift = At.Shift.AFTER))
    private void onUpdateCommandInfo(CallbackInfo ci) {
        if (pendingSuggestions == null) return;

        String fullInput = input.getValue();

        if (fullInput.startsWith("/")) {
            pendingSuggestions = pendingSuggestions.thenApply(originalSuggestions -> {
                List<String> list = originalSuggestions.getList().stream()
                        .map(Suggestion::getText)
                        .collect(Collectors.toCollection(ArrayList::new));

                CommandSuggestionEvent event = new CommandSuggestionEvent.Modify(fullInput.substring(1), list);
                MixinHelper.post(event);

                int start = originalSuggestions.getRange().getStart();

                SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(fullInput, start);
                for (String suggestion : event.getSuggestions()) {
                    suggestionsBuilder.suggest(suggestion);
                }

                return suggestionsBuilder.build();
            });
        }
    }
}
