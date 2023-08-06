package com.wynntils.handlers.item.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ShinyStatisticUpdateEvent extends Event {
    private final String statisticName;
    private final int existingStatistic;
    private final int newStatistic;
    private final ItemStack newItem;

    public ShinyStatisticUpdateEvent(ItemStack newItem, String statisticName, int existingStatistic, int newStatistic) {
        this.newItem = newItem;
        this.statisticName = statisticName;
        this.existingStatistic = existingStatistic;
        this.newStatistic = newStatistic;
    }

    public String getStatisticName() {
        return statisticName;
    }

    public int getExistingStatistic() {
        return existingStatistic;
    }

    public int getNewStatistic() {
        return newStatistic;
    }

    public ItemStack getNewItem() {
        return newItem;
    }
}
