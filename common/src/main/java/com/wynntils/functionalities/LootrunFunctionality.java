/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functionalities;

import com.wynntils.core.functionalities.FunctionalityBase;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.RenderLevelLastEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.wc.utils.ContainerUtils;
import com.wynntils.wc.utils.lootrun.LootrunUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootrunFunctionality extends FunctionalityBase {
    public LootrunFunctionality() {
        LootrunUtils.LOOTRUNS.mkdirs();
    }

    @SubscribeEvent
    public static void recordMovement(ClientTickEvent event) {
        if (event.getTickPhase() == ClientTickEvent.Phase.START) {
            LootrunUtils.recordMovementIfRecording();
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState block = event.getWorld().getBlockState(event.getPos());
        if (block.is(Blocks.CHEST)) {
            LootrunUtils.setLastChestIfRecording(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onOpen(ScreenOpenedEvent event) {
        if (ContainerUtils.isLootChest(event.getScreen())) {
            LootrunUtils.addChestIfRecording();
        }
    }

    @SubscribeEvent
    public static void onRenderLastLevel(RenderLevelLastEvent event) {
        LootrunUtils.render(event.getPoseStack());
    }
}
