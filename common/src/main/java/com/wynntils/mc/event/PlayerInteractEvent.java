/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class PlayerInteractEvent extends PlayerEvent {
    private final InteractionHand hand;
    private final BlockPos pos;

    @Nullable
    private final Direction face;

    private InteractionResult cancellationResult = InteractionResult.PASS;

    private PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
        super(Preconditions.checkNotNull(player, "Null player in PlayerInteractEvent!"));
        this.hand = Preconditions.checkNotNull(hand, "Null hand in PlayerInteractEvent!");
        this.pos = Preconditions.checkNotNull(pos, "Null position in PlayerInteractEvent!");
        this.face = face;
    }

    @Nonnull
    public InteractionHand getHand() {
        return this.hand;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return this.getPlayer().getItemInHand(this.hand);
    }

    @Nonnull
    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public Direction getFace() {
        return this.face;
    }

    public Level getWorld() {
        return this.getPlayer().getCommandSenderWorld();
    }

    public InteractionResult getCancellationResult() {
        return this.cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    @Cancelable
    public static class RightClickBlock extends PlayerInteractEvent {
        private Event.Result useBlock = Event.Result.DEFAULT;
        private Event.Result useItem = Event.Result.DEFAULT;
        private BlockHitResult hitVec;

        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
            super(player, hand, pos, hitVec.getDirection());
            this.hitVec = hitVec;
        }

        public Event.Result getUseBlock() {
            return this.useBlock;
        }

        public Event.Result getUseItem() {
            return this.useItem;
        }

        public BlockHitResult getHitVec() {
            return this.hitVec;
        }

        public void setUseBlock(Event.Result triggerBlock) {
            this.useBlock = triggerBlock;
        }

        public void setUseItem(Event.Result triggerItem) {
            this.useItem = triggerItem;
        }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            if (canceled) {
                this.useBlock = Event.Result.DENY;
                this.useItem = Event.Result.DENY;
            }
        }
    }
}
