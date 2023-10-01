/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.event.ProfessionNodeGatheredEvent;
import com.wynntils.models.profession.type.HarvestInfo;
import com.wynntils.models.profession.type.ProfessionProgress;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.TimedSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProfessionModel extends Model {
    // §7x1 [+3952§f Ⓒ§7 Woodcutting XP] §6[14.64%]
    private static final Pattern PROFESSION_NODE_EXPERIENCE_PATTERN = Pattern.compile(
            "(§.x[\\d\\.]+ )?(§.)?\\[\\+(§d)?(?<gain>\\d+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§7 (?<name>.+) XP\\] §6\\[(?<current>[\\d.]+)%\\]");

    // §2[§a+1§2 Oak Wood]
    private static final Pattern PROFESSION_NODE_HARVEST_PATTERN =
            Pattern.compile("§2\\[§a\\+\\d+§2 (?<type>.+) (?<material>.+)\\]");

    // §dx2.0 §7[+§d28 §fⒺ §7Scribing XP] §6[56%]
    private static final Pattern PROFESSION_CRAFT_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+) §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §7(?<name>.+) XP\\] §6\\[(?<current>[\\d.]+)%\\]");

    private static final Pattern PROFESSION_LEVELUP_PATTERN =
            Pattern.compile("§e\\s+You are now level (?<level>\\d+) in §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§e (?<name>.+)");

    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§8 \\[([\\d.]+)%\\]");

    private static final int GATHER_COOLDOWN_TIME = 60;
    private static final int PROFESSION_NODE_RESPAWN_TIME = 60;
    private static final int MAX_HARVEST_LABEL_AGE = 4000;
    private static final int TICKS_PER_TIMER_UPDATE = 10;

    @Persisted
    private final Storage<Integer> professionDryStreak = new Storage<>(0);

    private Pair<Long, MaterialItem> lastHarvestItemGain = Pair.of(0L, null);
    private HarvestInfo lastHarvest;

    private TimedSet<Position> gatheredNodes = new TimedSet<>(10, TimeUnit.SECONDS, true);
    private Map<ProfessionType, ProfessionProgress> professionProgressMap = new ConcurrentHashMap<>();
    private final Map<ProfessionType, TimedSet<Float>> rawXpGainInLastMinute = new HashMap<>();

    public ProfessionModel() {
        super(List.of());

        for (ProfessionType pt : ProfessionType.values()) {
            rawXpGainInLastMinute.put(pt, new TimedSet<>(1, TimeUnit.MINUTES, true));
        }
    }

    @SubscribeEvent
    public void onContainerSetSlot(ContainerSetSlotEvent.Post event) {
        Optional<MaterialItem> materialItem = Models.Item.asWynnItem(event.getItemStack(), MaterialItem.class);

        if (materialItem.isEmpty()) return;

        lastHarvestItemGain = Pair.of(System.currentTimeMillis(), materialItem.get());
    }

    @SubscribeEvent
    public void onLabelSpawn(EntityLabelChangedEvent event) {
        Matcher matcher = event.getName().getMatcher(PROFESSION_NODE_EXPERIENCE_PATTERN);

        if (matcher.matches()) {
            Vec3 entityPosition = event.getEntity().position();

            if (gatheredNodes.stream()
                    .anyMatch(position ->
                            PosUtils.isSame(position, event.getEntity().position()))) {
                // We already recorded this XP gain, ignore it.
                return;
            }

            gatheredNodes.put(entityPosition);

            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));

            ProfessionNodeGatheredEvent.LabelShown gatherEvent = new ProfessionNodeGatheredEvent.LabelShown();
            WynntilsMod.postEvent(gatherEvent);

            return;
        }

        matcher = event.getName().getMatcher(PROFESSION_NODE_HARVEST_PATTERN);
        if (matcher.matches()) {
            if (lastHarvestItemGain.a() + MAX_HARVEST_LABEL_AGE >= System.currentTimeMillis()
                    && lastHarvestItemGain.b() != null) {
                MaterialItem materialItem = lastHarvestItemGain.b();
                lastHarvest = new HarvestInfo(lastHarvestItemGain.a(), materialItem.getMaterialProfile());
                lastHarvestItemGain = Pair.of(0L, null);

                if (lastHarvest.materialProfile().getTier() == 3) {
                    professionDryStreak.store(0);
                } else {
                    professionDryStreak.store(professionDryStreak.get() + 1);
                }
            }
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
}
