/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.statemanaged;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.FileUtils;
import com.wynntils.wynn.model.LootrunModel;
import com.wynntils.wynn.screens.WynnScreenMatchers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
public class LootrunFeature extends StateManagedFeature {
    public static LootrunFeature INSTANCE;

    // TODO: Add textured path type
    //    @Config
    //    public PathType pathType = PathType.TEXTURED;

    @Config
    public CustomColor activePathColor = CommonColors.LIGHT_BLUE;

    @Config
    public CustomColor recordingPathColor = CommonColors.RED;

    @Config
    public boolean rainbowLootRun = false;

    @Config
    public int cycleDistance = 20; // TODO limit this later

    @Config
    public boolean showNotes = true;

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        FileUtils.mkdir(LootrunModel.LOOTRUNS);
    }

    @SubscribeEvent
    public void recordMovement(ClientTickEvent.Start event) {
        LootrunModel.recordMovementIfRecording();
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState block = event.getWorld().getBlockState(event.getPos());
        if (block.is(Blocks.CHEST)) {
            LootrunModel.setLastChestIfRecording(event.getPos());
        }
    }

    @SubscribeEvent
    public void onOpen(ScreenOpenedEvent event) {
        if (WynnScreenMatchers.isLootChest(event.getScreen())) {
            LootrunModel.addChestIfRecording();
        }
    }

    @SubscribeEvent
    public void onRenderLastLevel(RenderLevelEvent.Post event) {
        LootrunModel.render(event.getPoseStack());
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        LootrunModel.recompileLootrun(false);
    }

    public enum PathType {
        TEXTURED,
        LINE
    }
}
