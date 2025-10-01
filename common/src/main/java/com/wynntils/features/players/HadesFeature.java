/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterCommand;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@ConfigCategory(Category.PLAYERS)
public class HadesFeature extends Feature {
    @Persisted
    public final Config<Boolean> getOtherPlayerInfo = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithParty = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithFriends = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithGuild = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareGear = new Config<>(false);

    @RegisterCommand
    private final LiteralCommandNode<CommandSourceStack> toggleShareCommand =
            Commands.literal("sharegear").executes(this::toggleShareGear).build();

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "getOtherPlayerInfo" -> {
                if (getOtherPlayerInfo.get()) {
                    Services.Hades.tryResendWorldData();
                } else {
                    Services.Hades.resetHadesUsers();
                }
            }
            case "shareWithParty" -> {
                if (shareWithParty.get()) {
                    Models.Party.requestData();
                } else {
                    Services.Hades.resetSocialType(SocialType.PARTY);
                }
            }
            case "shareWithFriends" -> {
                if (shareWithFriends.get()) {
                    Models.Friends.requestData();
                } else {
                    Services.Hades.resetSocialType(SocialType.FRIEND);
                }
            }
            case "shareWithGuild" -> {
                if (shareWithGuild.get()) {
                    Models.Guild.requestGuildMembers();
                } else {
                    Services.Hades.resetSocialType(SocialType.GUILD);
                }
            }
            case "shareGear" -> {
                if (shareGear.get()) {
                    Services.Hades.refreshGear();
                } else {
                    Services.Hades.clearGearCache();
                }
            }
        }
    }

    private int toggleShareGear(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        shareGear.store(!shareGear.get());
        shareGear.touched();

        if (shareGear.get()) {
            McUtils.sendMessageToClient(Component.translatable("feature.wynntils.hades.shareGear.enabled")
                    .withStyle(ChatFormatting.GREEN));
            Services.Hades.refreshGear();
        } else {
            McUtils.sendMessageToClient(Component.translatable("feature.wynntils.hades.shareGear.disabled")
                    .withStyle(ChatFormatting.RED));
            Services.Hades.clearGearCache();
        }

        return 1;
    }
}
