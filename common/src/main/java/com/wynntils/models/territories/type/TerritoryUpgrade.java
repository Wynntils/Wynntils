/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.type;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

// Credits to fuy.gg for originally collecting this data.
public enum TerritoryUpgrade {
    // Guild Tower
    DAMAGE(
            "Damage",
            "Increases the damage the tower does",
            "Damage: +%s%",
            Items.IRON_SWORD,
            GuildResource.ORE,
            new Level[] {
                new Level(0L, 0d),
                new Level(100L, 40d),
                new Level(300L, 80d),
                new Level(600L, 120d),
                new Level(1200L, 160d),
                new Level(2400L, 200d),
                new Level(4800L, 240d),
                new Level(8400L, 280d),
                new Level(12000L, 320d),
                new Level(15600L, 360d),
                new Level(19200L, 400d),
                new Level(22800L, 440d)
            }),

    ATTACK(
            "Attack",
            "Increases the rate the tower does an attack",
            "Attacks per Second: +%s%",
            Items.RABBIT_HIDE,
            GuildResource.CROPS,
            new Level[] {
                new Level(0L, 0d),
                new Level(100L, 50d),
                new Level(300L, 100d),
                new Level(600L, 150d),
                new Level(1200L, 220d),
                new Level(2400L, 300d),
                new Level(4800L, 400d),
                new Level(8400L, 500d),
                new Level(12000L, 620d),
                new Level(15600L, 660d),
                new Level(19200L, 740d),
                new Level(22800L, 840d)
            }),

    HEALTH(
            "Health",
            "Increases the health the tower has",
            "Health: +%s%",
            Items.FERMENTED_SPIDER_EYE,
            GuildResource.WOOD,
            new Level[] {
                new Level(0L, 0d),
                new Level(100L, 50d),
                new Level(300L, 100d),
                new Level(600L, 150d),
                new Level(1200L, 220d),
                new Level(2400L, 300d),
                new Level(4800L, 400d),
                new Level(8400L, 520d),
                new Level(12000L, 640d),
                new Level(15600L, 760d),
                new Level(19200L, 880d),
                new Level(22800L, 1000d)
            }),

    DEFENCE(
            "Defence",
            "Increases the defense the tower has",
            "Defence: +%s%",
            Items.SHIELD,
            GuildResource.FISH,
            new Level[] {
                new Level(0L, 0d),
                new Level(100L, 300d),
                new Level(300L, 450d),
                new Level(600L, 525d),
                new Level(1200L, 600d),
                new Level(2400L, 650d),
                new Level(4800L, 690d),
                new Level(8400L, 720d),
                new Level(12000L, 740d),
                new Level(15600L, 760d),
                new Level(19200L, 780d),
                new Level(22800L, 800d)
            }),

    // Bonuses
    STRONGER_MINIONS(
            "Stronger Minions",
            "Buffs the minions that spawn when your territory is attacked",
            "Minion Damage: +%s%",
            Items.SKELETON_SKULL,
            GuildResource.WOOD,
            new Level[] {
                new Level(0L, 0d),
                new Level(200L, 150d),
                new Level(400L, 200d),
                new Level(800L, 250d),
                new Level(1600L, 300d)
            }),
    TOWER_MULTI_ATTACKS(
            "Tower Multi-Attacks",
            "Increases the number of players your Guild Tower can attack at once",
            "Max Targets: %s",
            Items.ARROW,
            GuildResource.FISH,
            new Level[] {new Level(0L, 1d), new Level(4800L, 2d)}),
    TOWER_AURA(
            "Tower Aura",
            "Cast an outward-moving Aura from the Tower and damaging players between 100% and 200% of the Tower's damage.",
            "Frequency: %ss",
            Items.ENDER_PEARL,
            GuildResource.CROPS,
            new Level[] {new Level(0L, 0d), new Level(800L, 24d), new Level(1600L, 18d), new Level(3200L, 12d)}),
    TOWER_VOLLEY(
            "Tower Volley",
            "Cast a volley of fireballs from the Tower damaging players between 100% and 200% of the Tower's damage.",
            "Frequency: %ss",
            Items.FIRE_CHARGE,
            GuildResource.ORE,
            new Level[] {new Level(0L, 0d), new Level(200L, 20d), new Level(400L, 15d), new Level(800L, 10d)}),
    GATHERING_EXPERIENCE(
            "Gathering Experience",
            "Guild members in this territory will gain bonus gathering XP",
            "Gathering XP: +%s%",
            Items.CARROT,
            GuildResource.WOOD,
            new Level[] {
                new Level(0L, 0d),
                new Level(600L, 10d),
                new Level(1300L, 20d),
                new Level(2000L, 30d),
                new Level(2700L, 40d),
                new Level(3400L, 50d),
                new Level(5500L, 60d),
                new Level(10000L, 80d),
                new Level(20000L, 100d)
            }),
    MOB_EXPERIENCE(
            "Mob Experience",
            "Guild members in this territory will receive more XP from mobs",
            "XP Bonus: +%s%",
            Items.SUNFLOWER,
            GuildResource.FISH,
            new Level[] {
                new Level(0L, 0d),
                new Level(600L, 10d),
                new Level(1200L, 20d),
                new Level(1800L, 30d),
                new Level(2400L, 40d),
                new Level(3000L, 50d),
                new Level(5000L, 60d),
                new Level(10000L, 80d),
                new Level(20000L, 100d)
            }),
    MOB_DAMAGE(
            "Mob Damage",
            "Guild members in this territory will deal more damage to mobs",
            "Damage Bonus: +%s%",
            Items.STONE_SWORD,
            GuildResource.CROPS,
            new Level[] {
                new Level(0L, 0d),
                new Level(600L, 10d),
                new Level(1200L, 20d),
                new Level(1800L, 40d),
                new Level(2400L, 60d),
                new Level(3000L, 80d),
                new Level(5000L, 120d),
                new Level(10000L, 160d),
                new Level(20000L, 200d)
            }),
    PVP_DAMAGE(
            "PvP Damage",
            "Guild members in this territory will deal more damage to players",
            "Damage Bonus: +%s%",
            Items.GOLDEN_SWORD,
            GuildResource.ORE,
            new Level[] {
                new Level(0L, 0d),
                new Level(600L, 5d),
                new Level(1200L, 10d),
                new Level(1800L, 15d),
                new Level(2400L, 20d),
                new Level(3000L, 25d),
                new Level(5000L, 40d),
                new Level(10000L, 65d),
                new Level(20000L, 80d)
            }),
    XP_SEEKING(
            "XP Seeking",
            "Your guild will gain XP while holding this territory",
            "Guild XP: +%s/h",
            Items.GLOWSTONE_DUST,
            GuildResource.EMERALDS,
            new Level[] {
                new Level(0L, 0d),
                new Level(100L, 36000d),
                new Level(200L, 66000d),
                new Level(400L, 120000d),
                new Level(800L, 228000d),
                new Level(1600L, 456000d),
                new Level(3200L, 900000d),
                new Level(6400L, 1740000d),
                new Level(9600L, 2580000d),
                new Level(12800L, 3360000d)
            }),
    TOME_SEEKING(
            "Tome Seeking",
            "Your guild will have a chance to find exclusive tomes while holding this territory",
            "Drop Chance: %s%/h",
            Items.ENCHANTED_BOOK,
            GuildResource.FISH,
            new Level[] {new Level(0L, 0d), new Level(400L, 0.15d), new Level(3200L, 1.2d), new Level(6400L, 2.4d)}),
    EMERALD_SEEKING(
            "Emerald Seeking",
            "Your guild will have a chance to find emeralds while holding this territory",
            "Drop Chance: %s%/h",
            Items.EMERALD_ORE,
            GuildResource.WOOD,
            new Level[] {
                new Level(0L, 0d),
                new Level(200L, 0.3d),
                new Level(800L, 3d),
                new Level(1600L, 6d),
                new Level(3200L, 12d),
                new Level(6400L, 24d)
            }),
    RESOURCE_STORAGE(
            "Larger Resource Storage",
            "Increases the storage limit for resources in this territory",
            "Storage Bonus: +%s%",
            Items.BREAD,
            GuildResource.EMERALDS,
            new Level[] {
                new Level(0L, 0d),
                new Level(400L, 100d),
                new Level(800L, 300d),
                new Level(2000L, 700d),
                new Level(5000L, 1400d),
                new Level(16000L, 3300d),
                new Level(48000L, 7900d)
            }),
    EMERALD_STORAGE(
            "Larger Emerald Storage",
            "Increases the storage limit for emeralds in this territory",
            "Storage Bonus: +%s%",
            Items.EMERALD_BLOCK,
            GuildResource.WOOD,
            new Level[] {
                new Level(0L, 0d),
                new Level(200L, 100d),
                new Level(400L, 300d),
                new Level(1000L, 700d),
                new Level(2500L, 1400d),
                new Level(8000L, 3300d),
                new Level(24000L, 7900d)
            }),
    EFFICIENT_RESOURCES(
            "Efficient Resources",
            "Increases the amount of resources this territory will produce",
            "Gathering Bonus: +%s%",
            Items.GOLDEN_PICKAXE,
            GuildResource.EMERALDS,
            new Level[] {
                new Level(0L, 0d),
                new Level(6000L, 50d),
                new Level(12000L, 100d),
                new Level(24000L, 150d),
                new Level(48000L, 200d),
                new Level(96000L, 250d),
                new Level(192000L, 300d)
            }),
    RESOURCE_RATE(
            "Resource Rate",
            "Decreases the time needed to produce resources on this territory",
            "Gathering Rate: %ss",
            Items.MUSHROOM_STEM,
            GuildResource.EMERALDS,
            new Level[] {new Level(0L, 4d), new Level(6000L, 3d), new Level(18000L, 2d), new Level(32000L, 1d)}),
    EFFICIENT_EMERALDS(
            "Efficient Emeralds",
            "Increases the amount of emeralds this territory will produce",
            "Emerald Bonus: +%s%",
            Items.EMERALD,
            GuildResource.ORE,
            new Level[] {new Level(0L, 0d), new Level(2000L, 35d), new Level(8000L, 100d), new Level(32000L, 300d)}),
    EMERALD_RATE(
            "Emerald Rate",
            "Decreases the time needed to produce emeralds on this territory",
            "Gather Rate: %ss",
            Items.EXPERIENCE_BOTTLE,
            GuildResource.CROPS,
            new Level[] {new Level(0L, 4d), new Level(2000L, 3d), new Level(8000L, 2d), new Level(32000L, 1d)});

    private final String name;
    private final String description;
    private final String bonusFormat;
    private final Item icon;
    private final GuildResource costResource;
    private final Level[] levels;

    TerritoryUpgrade(
            String name,
            String description,
            String bonusFormat,
            Item icon,
            GuildResource costResource,
            Level[] levels) {
        this.name = name;
        this.description = description;
        this.bonusFormat = bonusFormat;
        this.icon = icon;
        this.costResource = costResource;
        this.levels = levels;
    }

    public static TerritoryUpgrade fromName(String name) {
        for (TerritoryUpgrade upgrade : values()) {
            if (upgrade.getName().equalsIgnoreCase(name)) {
                return upgrade;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBonusFormat() {
        return bonusFormat;
    }

    public Item getIcon() {
        return icon;
    }

    public GuildResource getCostResource() {
        return costResource;
    }

    public Level[] getLevels() {
        return levels;
    }

    public record Level(long cost, double bonus) {}
}
