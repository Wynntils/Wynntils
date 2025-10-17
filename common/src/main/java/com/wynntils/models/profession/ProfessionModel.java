/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.profession.event.ProfessionXpGainEvent;
import com.wynntils.models.profession.label.CraftingStationLabelParser;
import com.wynntils.models.profession.label.GatheringNodeHarvestLabelInfo;
import com.wynntils.models.profession.label.GatheringNodeHarvestLabelParser;
import com.wynntils.models.profession.label.GatheringNodeLabelParser;
import com.wynntils.models.profession.type.HarvestInfo;
import com.wynntils.models.profession.type.ProfessionProgress;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.CappedValue;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ProfessionModel extends Model {
    // §dx2.0 §7[+§d28 §fⒺ §7Scribing XP] §6[56%]
    private static final Pattern PROFESSION_CRAFT_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+) §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §7(?<name>.+) XP\\] §6\\[(?<current>[\\d.]+)%\\]");

    private static final Pattern PROFESSION_LEVELUP_PATTERN =
            Pattern.compile("§e\\s+You are now level (?<level>\\d+) in §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§e (?<name>.+)");

    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§8 \\[([\\d.]+)%\\]");

    private static final int MAX_HARVEST_LABEL_AGE = 4000;

    /*
     * These values were taken from a community made spreadsheet that claims these values were provided by Salted at 1/30/2023
     * https://docs.google.com/spreadsheets/d/1ubp52M-3kJCkaKcs3VLtVWQTVfea2Aflg3tUNuyDEtw/edit?gid=1950989180#gid=1950989180
     */
    private static final int[] LEVEL_UP_XP_REQUIREMENTS = {
        30, 35, 42, 48, 56, 64, 74, 84, 96, 109, 123, 140, 158, 178, 200, 225, 253, 284, 319, 358, 401, 449, 502, 562,
        629, 703, 786, 878, 981, 1096, 1224, 1367, 1526, 1704, 1901, 2122, 2368, 2643, 2948, 3289, 3670, 4094, 4567,
        5094, 5682, 6337, 7068, 7882, 8791, 9804, 10933, 12193, 13597, 15162, 16908, 18855, 21025, 23445, 26143, 29151,
        32506, 36246, 40416, 45066, 50250, 56031, 62477, 69664, 77677, 86612, 96574, 107682, 120068, 133877, 149275,
        166444, 185587, 206932, 230731, 257267, 286855, 319845, 356629, 397643, 443374, 494364, 551218, 614610, 685292,
        764103, 851977, 949956, 1059203, 1181014, 1316832, 1468270, 1637123, 1825394, 2035317, 2269380, 2530361,
        2821354, 3145812, 3507582, 3910956, 4360718, 4862203, 5421358, 6044816, 6739972, 7515071, 8379306, 9342928,
        10417367, 11615366, 12951135, 14440517, 16101179, 17952817, 20017392, 22319395, 24886127, 27748034, 30939059,
        34497053, 38464216, 42887603, 47819680, 53318945, 59450625, 66287449
    };

    @Persisted
    private final Storage<Integer> professionDryStreak = new Storage<>(0);

    private long lastGatherTime = 0L;
    private HarvestInfo lastHarvest;

    private final TimedSet<Integer> harvestIds = new TimedSet<>(MAX_HARVEST_LABEL_AGE, TimeUnit.MILLISECONDS, true);
    private Map<ProfessionType, ProfessionProgress> professionProgressMap = new ConcurrentHashMap<>();
    private final Map<ProfessionType, TimedSet<Float>> rawXpGainInLastMinute = new HashMap<>();
    private ProfessionType lastProfessionXpGain;

    public ProfessionModel() {
        super(List.of());

        Handlers.Label.registerParser(new GatheringNodeLabelParser());
        Handlers.Label.registerParser(new CraftingStationLabelParser());
        Handlers.Label.registerParser(new GatheringNodeHarvestLabelParser());

        for (ProfessionType pt : ProfessionType.values()) {
            rawXpGainInLastMinute.put(pt, new TimedSet<>(1, TimeUnit.MINUTES, true));
        }
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof GatheringNodeHarvestLabelInfo gatheringInfo) {
            if (harvestIds.stream()
                    .anyMatch(id -> id == event.getLabelInfo().getEntity().getId())) {
                if (gatheringInfo.getMaterialProfile().isEmpty()) return;

                if (lastGatherTime + MAX_HARVEST_LABEL_AGE >= System.currentTimeMillis()) {
                    lastHarvest = new HarvestInfo(
                            lastGatherTime, gatheringInfo.getMaterialProfile().get(), gatheringInfo.getXpGain());
                    lastGatherTime = 0L;

                    if (lastHarvest.materialProfile().getTier() == 3) {
                        professionDryStreak.store(0);
                    } else {
                        professionDryStreak.store(professionDryStreak.get() + 1);
                    }
                }

                return;
            }
            harvestIds.put(gatheringInfo.getEntity().getId());
            lastGatherTime = System.currentTimeMillis();
            lastProfessionXpGain = gatheringInfo.getProfessionType();
            WynntilsMod.postEvent(new ProfessionXpGainEvent(
                    gatheringInfo.getProfessionType(), gatheringInfo.getXpGain(), gatheringInfo.getCurrentXp()));
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText message = event.getMessage();

        Matcher craftMatcher = message.getMatcher(PROFESSION_CRAFT_PATTERN);
        if (craftMatcher.matches()) {
            lastProfessionXpGain = ProfessionType.fromString(craftMatcher.group("name"));
            ProfessionXpGainEvent xpGainEvent = new ProfessionXpGainEvent(
                    ProfessionType.fromString(craftMatcher.group("name")),
                    Float.parseFloat(craftMatcher.group("gain")),
                    Float.parseFloat(craftMatcher.group("current")));
            WynntilsMod.postEvent(xpGainEvent);
            if (xpGainEvent.isCanceled()) {
                event.cancelChat();
            }
            return;
        }

        Matcher levelUpMatcher = message.getMatcher(PROFESSION_LEVELUP_PATTERN);
        if (levelUpMatcher.matches()) {
            updateLevel(
                    ProfessionType.fromString(levelUpMatcher.group("name")),
                    Integer.parseInt(levelUpMatcher.group("level")));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onXpGain(ProfessionXpGainEvent event) {
        ProfessionType profession = event.getProfession();
        ProfessionProgress oldValue = professionProgressMap.getOrDefault(profession, ProfessionProgress.NO_PROGRESS);

        // We leveled up, but we don't know how many times.
        // Set the progress, level will be parsed from other messages.
        float newPercentage = event.getCurrentXpPercentage();
        if (newPercentage == 100) {
            newPercentage = 0;
        }

        professionProgressMap.put(profession, new ProfessionProgress(oldValue.level(), newPercentage));

        rawXpGainInLastMinute.get(profession).put(event.getGainedXpRaw());
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

    public int getXpPointsNeededToLevelUp(ProfessionType type) {
        int levelIndex = getLevel(type) - 1;
        if (levelIndex >= LEVEL_UP_XP_REQUIREMENTS.length) {
            return Integer.MAX_VALUE;
        }
        if (levelIndex < 0) {
            return 0;
        }
        return LEVEL_UP_XP_REQUIREMENTS[levelIndex];
    }

    public CappedValue getXP(ProfessionType type) {
        int maxXP = getXpPointsNeededToLevelUp(type);
        return CappedValue.fromProgress((float) (getProgress(type) / 100), maxXP);
    }

    public Optional<ProfessionType> getLastProfessionXpGain() {
        return Optional.ofNullable(lastProfessionXpGain);
    }
}
