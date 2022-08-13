/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.chat.ChatModel;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.NpcDialogEvent;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@StartDisabled
public class NpcDialogAutoProgressFeature extends UserFeature {

    public static NpcDialogAutoProgressFeature INSTANCE;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Runnable sendShiftRunnable = () -> {
        McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
    };

    private ScheduledFuture<?> lastScheduledFuture = null;

    @Config
    public static int dialogAutoProgressTime = 2000; // Milliseconds

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ChatModel.class);
        ChatModel.addNpcDialogExtractionDependent(this);
    }

    @SubscribeEvent
    public void onNpcDialog(NpcDialogEvent event) {
        if (lastScheduledFuture != null) {
            lastScheduledFuture.cancel(true);
        }

        McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                McUtils.player(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));

        if (event.getCodedDialog() == null) return;

        lastScheduledFuture = executor.schedule(sendShiftRunnable, dialogAutoProgressTime, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (event.getKey() != GLFW.GLFW_KEY_SPACE) return;

        if (lastScheduledFuture != null) {
            lastScheduledFuture.cancel(true);
        }
    }

    public Optional<Long> millisecondsUntilProgress() {
        if (lastScheduledFuture == null) {
            return Optional.empty();
        }

        if (lastScheduledFuture.isCancelled()) {
            return Optional.empty();
        }

        return Optional.of(lastScheduledFuture.getDelay(TimeUnit.MILLISECONDS));
    }
}
