/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.wynntils.core.components.Model;
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
    private static final Pattern PROFESSION_NODE_HARVERSTED_PATTERN =
            Pattern.compile("§7\\[\\+(?<gain>\\d+)§f [ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§7 (?<name>.+)§7 XP\\] §6\\[(?<current>\\d+)%\\]");

    private static final Pattern PROFESSION_CRAFT_PATTERN = Pattern.compile(
            "§7\\[\\+(?<gain>\\d+) §r§f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] §r§7(?<name>.+) XP\\] §r§6\\[(?<current>\\d+)%\\]");

    private static final Pattern PROFESSION_LEVELUP_PATTERN = Pattern.compile(
            "§e                   You are now level (?<level>\\d+) in §r§f[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ]§r§e (?<name>.+)");

    private static final Pattern INFO_MENU_PROFESSION_LORE_PATTERN =
            Pattern.compile("§6- §r§7[ⓀⒸⒷⒿⒺⒹⓁⒶⒼⒻⒾⒽ] Lv. (\\d+) (.+)§r§8 \\[([\\d.]+)%\\]");

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
        Matcher matcher = PROFESSION_NODE_HARVERSTED_PATTERN.matcher(event.getName());

        if (matcher.matches()) {
            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        String codedMessage = event.getOriginalCodedMessage();

        Matcher matcher = PROFESSION_CRAFT_PATTERN.matcher(codedMessage);

        if (matcher.matches()) {
            updatePercentage(
                    ProfessionType.fromString(matcher.group("name")),
                    Float.parseFloat(matcher.group("current")),
                    Float.parseFloat(matcher.group("gain")));
        }

        matcher = PROFESSION_LEVELUP_PATTERN.matcher(codedMessage);

        if (matcher.matches()) {
            updateLevel(ProfessionType.fromString(matcher.group("name")), Integer.parseInt(matcher.group("level")));
        }
    }

    public void resetValueFromItem(ItemStack professionInfoItem) {
        Map<ProfessionType, ProfessionProgress> levels = new ConcurrentHashMap<>();
        List<String> professionLore = LoreUtils.getLore(professionInfoItem);
        for (String line : professionLore) {
            Matcher matcher = INFO_MENU_PROFESSION_LORE_PATTERN.matcher(line);

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

    public void updatePercentage(ProfessionType type, float newPercentage, float xpGain) {
        ProfessionProgress oldValue = professionProgressMap.getOrDefault(type, ProfessionProgress.NO_PROGRESS);

        professionProgressMap.put(type, new ProfessionProgress(oldValue.level(), newPercentage));

        rawXpGainInLastMinute.get(type).put(xpGain);
    }

    public void updateLevel(ProfessionType type, int newLevel) {
        ProfessionProgress oldValue = professionProgressMap.getOrDefault(type, ProfessionProgress.NO_PROGRESS);

        // We don't know the progress, so we set it to 0.
        professionProgressMap.put(type, new ProfessionProgress(newLevel, 0));
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
