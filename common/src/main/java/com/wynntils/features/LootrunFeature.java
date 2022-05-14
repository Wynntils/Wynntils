/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.RenderLevelLastEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.wc.utils.lootrun.LootrunUtils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootrunFeature extends Feature {
    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        LootrunUtils.LOOTRUNS.mkdirs();
    }

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return true;
    }

    @Override
    protected void onDisable() {
        WynntilsMod.getEventBus().unregister(this);
    }

    @Override
    public MutableComponent getNameComponent() {
        return new TranslatableComponent("feature.wynntils.lootrunFeature.name");
    }

    @SubscribeEvent
    public void recordMovement(ClientTickEvent event) {
        if (event.getTickPhase() == ClientTickEvent.Phase.START) {
            LootrunUtils.recordMovementIfRecording();
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState block = event.getWorld().getBlockState(event.getPos());
        if (block.is(Blocks.CHEST)) {
            LootrunUtils.setLastChestIfRecording(event.getPos());
        }
    }

    @SubscribeEvent
    public void onOpen(ScreenOpenedEvent event) {
        if (event.getScreen() instanceof ContainerScreen screen) {
            if (screen.getTitle().getString().contains("Loot Chest ")) {
                LootrunUtils.addChestIfRecording();
            }
        }
    }

    @SubscribeEvent
    public void onRenderLastLevel(RenderLevelLastEvent event) {
        LootrunUtils.render(event.getPoseStack());
    }
}
