/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.commands;

import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

public class ClientCommandSourceStack extends CommandSourceStack {
    public ClientCommandSourceStack(LocalPlayer player) {
        super(
                new ClientCommandSource(),
                player.position(),
                player.getRotationVector(),
                null,
                PermissionSet.NO_PERMISSIONS,
                null,
                player);
    }

    @Override
    public HolderLookup.Provider holderLookups() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }

    @Override
    public HolderGetter.Provider holderGetters() {
        return holderLookups();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return Minecraft.getInstance().getConnection().enabledFeatures();
    }

    private static class ClientCommandSource implements CommandSource {
        @Override
        public void sendSystemMessage(Component component) {
            McUtils.sendWynntilsPrefixMessage(component);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }
    }
}
