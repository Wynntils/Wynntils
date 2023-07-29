/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.storage.RegisterStorage;
import com.wynntils.core.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.event.ProfessionNodeGatheredEvent;
import com.wynntils.models.profession.type.HarvestInfo;
import com.wynntils.models.profession.type.ProfessionProgress;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.worlds.BombModel;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.BombType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.type.TimedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProfessionModel extends Model {
    // §7[+36§f Ⓙ§7 Farming XP] §6[9%]
    // §dx2.0 §7[+§d93§f Ⓙ§7 Farming XP] §6[9%]
    private static final Pattern PROFESSION_NODE_EXPERIENCE_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§7 (?<name>.+) XP\\] §6\\[(?<current>\\d+)%\\]");

    // §2[§a+1§2 Oak Wood]
    private static final Pattern PROFESSION_NODE_HARVEST_PATTERN =
            Pattern.compile("§2\\[§a\\+\\d+§2 (?<type>.+) (?<material>.+)\\]");

    // §dx2.0 §7[+§d28 §fⒺ §7Scribing XP] §6[56%]
    private static final Pattern PROFESSION_CRAFT_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+) §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §7(?<name>.+) XP\\] §6\\[(?<current>\\d+)%\\]");

    private static final Pattern PROFESSION_LEVELUP_PATTERN =
            Pattern.compile("§e\\s+You are now level (?<level>\\d+) in §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§e (?<name>.+)");

    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§8 \\[([\\d.]+)%\\]");

    // This should be 60, but Wynn is buggy and it is 70 (35 with profession speed)
    public static final int GATHER_COOLDOWN_TIME = 70;
    private static final int PROFESSION_NODE_RESPAWN_TIME = 60;
    private static final int MAX_HARVEST_LABEL_AGE = 1000;
    private static final int TICKS_PER_TIMER_UPDATE = 10;

    @RegisterStorage
    private final Storage<Integer> professionDryStreak = new Storage<>(0);

    private long lastHarvestLabel = 0;
    private HarvestInfo lastHarvest;

    private Map<ProfessionType, ProfessionProgress> professionProgressMap = new ConcurrentHashMap<>();
    private final Map<ProfessionType, TimedSet<Float>> rawXpGainInLastMinute = new HashMap<>();

    private final List<ProfessionTimerArmorStand> professionTimerArmorStands = new LinkedList<>();
    private int tickTimer = 0;

    public ProfessionModel(CharacterModel characterModel, WorldStateModel worldStateModel, BombModel bombModel) {
        super(List.of(characterModel, worldStateModel, bombModel));
        for (ProfessionType pt : ProfessionType.values()) {
            rawXpGainInLastMinute.put(pt, new TimedSet<>(1, TimeUnit.MINUTES, true));
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        Optional<MaterialItem> materialItem = Models.Item.asWynnItem(event.getItemStack(), MaterialItem.class);

        if (materialItem.isEmpty()) return;

        if (lastHarvestLabel + MAX_HARVEST_LABEL_AGE >= System.currentTimeMillis()) {
            lastHarvest = new HarvestInfo(lastHarvestLabel, materialItem.get().getMaterialProfile());
            lastHarvestLabel = 0;

            if (lastHarvest.materialProfile().getTier() == 3) {
                professionDryStreak.store(0);
            } else {
                professionDryStreak.store(professionDryStreak.get() + 1);
            }
        }
    }

    @SubscribeEvent
    public void onLabelSpawn(EntityLabelChangedEvent event) {
        Matcher matcher = event.getName().getMatcher(PROFESSION_NODE_EXPERIENCE_PATTERN);

        if (matcher.matches()) {
            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));

            ProfessionNodeGatheredEvent.LabelShown gatherEvent = new ProfessionNodeGatheredEvent.LabelShown();
            WynntilsMod.postEvent(gatherEvent);
            if (gatherEvent.shouldAddCooldownArmorstand()) {
                boolean professionSpeed = Models.Bomb.isBombActive(BombType.PROFESSION_SPEED);
                professionTimerArmorStands.add(new ProfessionTimerArmorStand(
                        event.getEntity(),
                        professionSpeed ? PROFESSION_NODE_RESPAWN_TIME / 2 : PROFESSION_NODE_RESPAWN_TIME));
            }

            return;
        }

        matcher = event.getName().getMatcher(PROFESSION_NODE_HARVEST_PATTERN);
        if (matcher.matches()) {
            lastHarvestLabel = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        StyledText codedMessage = event.getOriginalStyledText();

        Matcher matcher = codedMessage.getMatcher(PROFESSION_CRAFT_PATTERN);

        if (matcher.matches()) {
            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));
            return;
        }

        matcher = codedMessage.getMatcher(PROFESSION_LEVELUP_PATTERN);

        if (matcher.matches()) {
            updateLevel(ProfessionType.fromString(matcher.group("name")), Integer.parseInt(matcher.group("level")));
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        professionTimerArmorStands.forEach(armorStand ->
                McUtils.mc().level.removeEntity(armorStand.entity.getId(), Entity.RemovalReason.DISCARDED));

        professionTimerArmorStands.clear();
        tickTimer = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (tickTimer % TICKS_PER_TIMER_UPDATE != 0) {
            tickTimer++;
            return;
        }

        tickTimer = 0;

        List<ProfessionTimerArmorStand> removedElements = new ArrayList<>();

        for (ProfessionTimerArmorStand armorStand : professionTimerArmorStands) {
            boolean toBeRemoved = armorStand.tick();

            if (toBeRemoved) {
                removedElements.add(armorStand);
            }
        }

        professionTimerArmorStands.removeAll(removedElements);
    }

    public void resetValueFromItem(ItemStack professionInfoItem) {
        Map<ProfessionType, ProfessionProgress> levels = new ConcurrentHashMap<>();
        List<StyledText> professionLore = LoreUtils.getLore(professionInfoItem);
        for (StyledText line : professionLore) {
            Matcher matcher = line.getMatcher(INFO_MENU_PROFESSION_LORE_PATTERN);

            if (matcher.matches()) {
                // NOTE: When writing this, progress was quite a bit off in this lore. Still, parse it and use it while
                // we don't have accurate info.
                levels.put(
                        ProfessionType.fromString(matcher.group(2)),
                        new ProfessionProgress(Integer.parseInt(matcher.group(1)), Float.parseFloat(matcher.group(3))));
            }
        }

        for (ProfessionType value : ProfessionType.values()) {
            levels.putIfAbsent(value, ProfessionProgress.NO_PROGRESS);
        }

        professionProgressMap = levels;
    }

    private void updatePercentage(ProfessionType type, float newPercentage, float xpGain) {
        ProfessionProgress oldValue = professionProgressMap.getOrDefault(type, ProfessionProgress.NO_PROGRESS);

        // We leveled up, but we don't know how many times.
        // Set the progress, level will be parsed from other messages.
        if (newPercentage == 100) {
            newPercentage = 0;
        }

        professionProgressMap.put(type, new ProfessionProgress(oldValue.level(), newPercentage));

        rawXpGainInLastMinute.get(type).put(xpGain);
    }

    private void updateLevel(ProfessionType type, int newLevel) {
        ProfessionProgress oldValue = professionProgressMap.getOrDefault(type, ProfessionProgress.NO_PROGRESS);

        professionProgressMap.put(type, new ProfessionProgress(newLevel, oldValue.progress()));
    }

    public int getLevel(ProfessionType type) {
        return professionProgressMap
                .getOrDefault(type, ProfessionProgress.NO_PROGRESS)
                .level();
    }

    public double getProgress(ProfessionType type) {
        return professionProgressMap
                .getOrDefault(type, ProfessionProgress.NO_PROGRESS)
                .progress();
    }

    public Optional<HarvestInfo> getLastHarvest() {
        return Optional.ofNullable(lastHarvest);
    }

    public Map<ProfessionType, TimedSet<Float>> getRawXpGainInLastMinute() {
        return Collections.unmodifiableMap(rawXpGainInLastMinute);
    }

    public int getProfessionDryStreak() {
        return professionDryStreak.get();
    }

    public int getGatherCooldownTime() {
        return Models.Bomb.isBombActive(BombType.PROFESSION_SPEED) ? GATHER_COOLDOWN_TIME / 2 : GATHER_COOLDOWN_TIME;
    }

    private static final class ProfessionTimerArmorStand {
        private final Entity entity;
        private final int length;

        private long startTime;
        private long endTime;

        private ProfessionTimerArmorStand(Entity positionBaseEntity, int length) {
            this.entity = createArmorStandAt(positionBaseEntity);
            this.length = length;
            this.startTime = System.currentTimeMillis();
            this.endTime = startTime + length * 1000L;

            McUtils.mc().level.putNonPlayerEntity(entity.getId(), entity);
        }

        public boolean tick() {
            int remaining = Math.round((endTime - System.currentTimeMillis()) / 1000L);
            entity.setCustomName(RenderedStringUtils.getPercentageComponent(length - remaining, length, 8, true, "s"));

            boolean toBeRemoved = remaining <= 0;

            if (toBeRemoved) {
                McUtils.mc().level.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
            }

            return toBeRemoved;
        }

        private static Entity createArmorStandAt(Entity copiedEntity) {
            Entity entity = EntityType.ARMOR_STAND.create(McUtils.mc().level);
            entity.copyPosition(copiedEntity);
            entity.setPos(entity.position().add(0, -1, 0));
            entity.setCustomNameVisible(true);
            entity.setInvisible(true);
            entity.setInvulnerable(true);

            return entity;
        }
    }
}
