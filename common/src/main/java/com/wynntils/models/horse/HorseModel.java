/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.horse;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class HorseModel extends Model {
    public HorseModel() {
        super(List.of());
    }

    public Optional<HorseItem> getHorse() {
        int horseSlot = findHorseSlotNum();
        if (horseSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(horseSlot), HorseItem.class);
    }

    public Optional<CappedValue> calculateNextLevelMinutes() {
        if (getHorse().isEmpty()) return Optional.empty();

        HorseItem horseItem = getHorse().get();

        if (horseItem.getLevel() == CappedValue.EMPTY || horseItem.getXp() == CappedValue.EMPTY)
            return Optional.empty();
        if (horseItem.getLevel().current() == horseItem.getLevel().max()) return Optional.empty();

        // This is based off of a formula from https://wynncraft.wiki.gg/wiki/Horses#Levels
        double levelProgress = (horseItem.getLevel().current() + (2f / 3f)) / 2;
        double xpProgress = 100.0 - horseItem.getXp().current();

        double result = levelProgress * (xpProgress / 100.0) * 100.0;
        double resultMax = levelProgress * 100.0;

        return Optional.of(new CappedValue((int) Math.ceil(result), (int) Math.ceil(resultMax)));
    }

    public int findHorseSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            if (Models.Item.asWynnItem(itemStack, HorseItem.class).isPresent()) {
                return slotNum;
            }
        }
        return -1;
    }

    public AbstractHorse searchForHorseNearby(Player player, int searchRadius) {
        List<AbstractHorse> horses = McUtils.mc()
                .level
                .getEntitiesOfClass(
                        AbstractHorse.class,
                        new AABB(
                                player.getX() - searchRadius,
                                player.getY() - searchRadius,
                                player.getZ() - searchRadius,
                                player.getX() + searchRadius,
                                player.getY() + searchRadius,
                                player.getZ() + searchRadius));

        return horses.stream()
                .filter(horse -> isPlayersHorse(horse, player))
                .findFirst()
                .orElse(null);
    }

    private boolean isPlayersHorse(AbstractHorse horse, Player player) {
        if (horse == null) return false;
        Component horseName = horse.getCustomName();
        if (horseName == null) return false;

        String playerName = player.getName().getString();
        StyledText defaultName = StyledText.fromString("§f" + playerName + "§7" + "'s horse");
        StyledText codedHorseName = StyledText.fromComponent(horseName);
        return defaultName.equals(codedHorseName) || codedHorseName.endsWith("§7" + " [" + playerName + "]");
    }
}
