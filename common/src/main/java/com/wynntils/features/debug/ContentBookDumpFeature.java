/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.ActivityRequirements;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@StartDisabled
@ConfigCategory(Category.DEBUG)
public class ContentBookDumpFeature extends Feature {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");

    @RegisterKeyBind
    private final KeyBind dumpContentBook =
            new KeyBind("Dump Content Book", GLFW.GLFW_KEY_UNKNOWN, true, this::dumpContentBook);

    private List<DumpableActivityInfo> currentDump = List.of();

    private void dumpContentBook() {
        currentDump = new ArrayList<>();

        Models.Activity.scanContentBook(ActivityType.ALL, (activityInfos, progress) -> {
            currentDump.addAll(activityInfos.stream()
                    .map(DumpableActivityInfo::fromActivityInfo)
                    .toList());

            saveToDisk();

            // Free up memory
            currentDump = List.of();
        });
    }

    private void saveToDisk() {
        // Save the dump to a file
        Map<ActivityType, List<DumpableActivityInfo>> mappedActivities = getMappedDumpedActivities();

        JsonElement element = Managers.Json.GSON.toJsonTree(mappedActivities);

        String fileName = "content_book_dump.json";
        File jsonFile = new File(SAVE_FOLDER, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved content book dump to " + jsonFile.getAbsolutePath()));
    }

    // This method creates a consisnent mapping and ordering for the dumped activities
    private Map<ActivityType, List<DumpableActivityInfo>> getMappedDumpedActivities() {
        Map<ActivityType, List<DumpableActivityInfo>> mappedDumpedActivities = new EnumMap<>(ActivityType.class);
        for (ActivityType value : ActivityType.values()) {
            mappedDumpedActivities.put(value, new ArrayList<>());
        }

        for (DumpableActivityInfo activityInfo : currentDump) {
            ActivityType type = activityInfo.type();

            // Merge STORYLINE_QUEST and QUEST
            if (type == ActivityType.STORYLINE_QUEST) {
                type = ActivityType.QUEST;
            }

            mappedDumpedActivities.get(type).add(activityInfo);
        }

        // Remove unused types
        mappedDumpedActivities.remove(ActivityType.ALL);
        mappedDumpedActivities.remove(ActivityType.STORYLINE_QUEST);

        for (List<DumpableActivityInfo> value : mappedDumpedActivities.values()) {
            value.sort(Comparator.comparing(DumpableActivityInfo::name));
        }

        return mappedDumpedActivities;
    }

    private record DumpableActivityInfo(
            ActivityType type,
            String name,
            String specialInfo,
            String description,
            ActivityLength length,
            String lengthInfo,
            ActivityDifficulty difficulty,
            DumpableActivityRequirements requirements,
            List<String> rewards,
            Location location) {
        public static DumpableActivityInfo fromActivityInfo(ActivityInfo activityInfo) {
            return new DumpableActivityInfo(
                    activityInfo.type(),
                    activityInfo.name(),
                    activityInfo.specialInfo().orElse(""),
                    activityInfo.description().map(StyledText::getString).orElse(""),
                    activityInfo.length().orElse(null),
                    activityInfo.lengthInfo().orElse(""),
                    activityInfo.difficulty().orElse(null),
                    DumpableActivityRequirements.fromActivityRequirements(activityInfo.requirements()),
                    activityInfo.rewards(),
                    StyledTextUtils.extractLocation(activityInfo.description().orElse(StyledText.EMPTY))
                            .orElse(null));
        }

        public record DumpableActivityRequirements(
                int level, Map<ProfessionType, Integer> professionLevels, List<String> quests) {
            public static DumpableActivityRequirements fromActivityRequirements(ActivityRequirements requirements) {
                return new DumpableActivityRequirements(
                        requirements.level().a(),
                        requirements.professionLevels().stream()
                                .collect(Collectors.toMap(o -> o.a().a(), o -> o.a().b())),
                        requirements.quests().stream().map(Pair::a).toList());
            }
        }
    }
}
