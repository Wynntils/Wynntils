/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.horse;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class MountModel extends Model {
    public MountModel() {
        super(List.of());
    }

    // Sourced from https://desmos.com/calculator/rrckinnsjo
    private static final Map<Integer, Integer> MAX_LEVEL_TIMES = Map.of(
            10, 1530,
            15, 3430,
            20, 6080,
            25, 9480,
            30, 13630,
            35, 18530,
            40, 24180);

    public Optional<MountItem> getMount() {
        int mountSlot = findMountSlotNum();
        if (mountSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(mountSlot), MountItem.class);
    }

    public Optional<Integer> calculateNextLevelSeconds() {
        Optional<MountItem> optionalMount = getMount();
        if (optionalMount.isEmpty()) return Optional.empty();

        MountItem horseItem = optionalMount.get();

        if (horseItem.getSpeed() == CappedValue.EMPTY || horseItem.getEnergy() == CappedValue.EMPTY) {
            return Optional.empty();
        }
        if (horseItem.getSpeed().current() == horseItem.getSpeed().max()) return Optional.empty();

        // This is based off of a formula from https://wynncraft.wiki.gg/wiki/Horses#Levels
        double levelProgress = (horseItem.getSpeed().current() + (2.0 / 3.0)) / 2.0;
        double xpProgress = 100.0 - horseItem.getEnergy().current();

        double result = levelProgress * (xpProgress / 100.0) * 60.0;

        return Optional.of((int) Math.ceil(result));
    }

    public Optional<CappedValue> calculateNextLevelCumulativeSeconds() {
        Optional<MountItem> optionalHorse = getMount();
        if (optionalHorse.isEmpty()) return Optional.empty();

        MountItem horseItem = optionalHorse.get();

        if (horseItem.getSpeed() == CappedValue.EMPTY || horseItem.getEnergy() == CappedValue.EMPTY) {
            return Optional.empty();
        }
        if (MAX_LEVEL_TIMES.get(horseItem.getSpeed().max()) == null) return Optional.empty();

        double result = 0;

        double resultMax = MAX_LEVEL_TIMES.get(horseItem.getSpeed().max());

        for (int levelNumber = 1; levelNumber != horseItem.getSpeed().current() + 1; levelNumber++) {
            // This is based off of a formula from https://wynncraft.wiki.gg/wiki/Horses#Levels
            double levelProgress = (levelNumber + (2.0 / 3.0)) / 2.0;
            double xpProgress = 100.0 - horseItem.getEnergy().current();

            result += levelProgress * 60.0;

            if (levelNumber == horseItem.getSpeed().current()) {
                result -= (levelProgress * (xpProgress / 100.0) * 60.0);
            }
        }

        if (horseItem.getSpeed().current() == horseItem.getSpeed().max()) {
            result = resultMax;
        }

        return Optional.of(new CappedValue((int) Math.ceil(result), (int) Math.ceil(resultMax)));
    }

    public int findMountSlotNum() {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            if (Models.Item.asWynnItem(itemStack, MountItem.class).isPresent()) {
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
