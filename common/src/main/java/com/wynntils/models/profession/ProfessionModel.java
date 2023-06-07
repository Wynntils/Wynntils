/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.profession.type.ProfessionProgress;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.TimedSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProfessionModel extends Model {
    // §7[+36§f Ⓙ§7 Farming§7 XP] §6[9%]
    // §dx2.0 §7[+§d93§f Ⓙ§7 Farming§7 XP] §6[9%]
    private static final Pattern PROFESSION_NODE_HARVERSTED_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§7 (?<name>.+)§7 XP\\] §6\\[(?<current>\\d+)%\\]");

    // §dx2.0 §r§7[+§r§d28 §r§fⒺ §r§7Scribing XP] §r§6[56%]
    private static final Pattern PROFESSION_CRAFT_PATTERN = Pattern.compile(
            "(§dx[\\d\\.]+ )?§7\\[\\+(§d)?(?<gain>\\d+) §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §7(?<name>.+) XP\\] §6\\[(?<current>\\d+)%\\]");

    private static final Pattern PROFESSION_LEVELUP_PATTERN =
            Pattern.compile("§e\\s+You are now level (?<level>\\d+) in §f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§e (?<name>.+)");

    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§8 \\[([\\d.]+)%\\]");

    private Map<ProfessionType, ProfessionProgress> professionProgressMap = new ConcurrentHashMap<>();
    private final Map<ProfessionType, TimedSet<Float>> rawXpGainInLastMinute = new HashMap<>();

    public ProfessionModel(CharacterModel characterModel) {
        super(List.of(characterModel));
        for (ProfessionType pt : ProfessionType.values()) {
            rawXpGainInLastMinute.put(pt, new TimedSet<>(1, TimeUnit.MINUTES, true));
        }
    }

    @SubscribeEvent
    public void onLabelSpawn(EntityLabelChangedEvent event) {
        Matcher matcher = event.getName().getMatcher(PROFESSION_NODE_HARVERSTED_PATTERN);

        if (matcher.matches()) {
            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));
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

    public Map<ProfessionType, TimedSet<Float>> getRawXpGainInLastMinute() {
        return Collections.unmodifiableMap(rawXpGainInLastMinute);
    }
}
