/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.models.players.label.GuildSeasonLeaderboardHeaderLabelParser;
import com.wynntils.models.players.label.GuildSeasonLeaderboardLabelParser;
import com.wynntils.models.players.profile.GuildProfile;
import com.wynntils.models.players.type.GuildInfo;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildModel extends Model {
    private static final Gson GUILD_PROFILE_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GuildProfile.class, new GuildProfile.GuildProfileDeserializer())
            .registerTypeHierarchyAdapter(GuildInfo.class, new GuildInfo.GuildDeserializer())
            .create();

    // Test in GuildModel_GUILD_NAME_MATCHER
    private static final Pattern GUILD_NAME_MATCHER =
            Pattern.compile("^§[3a](§l)?(?<name>[a-zA-Z\\s]+?)(§b)? \\[[a-zA-Z]{3,4}]$");

    // Test in GuildModel_GUILD_RANK_MATCHER
    private static final Pattern GUILD_RANK_MATCHER =
            Pattern.compile("^§7Rank: §f(Recruit|Recruiter|Captain|Strategist|Chief|Owner)$");

    // Test in GuildModel_MSG_LEFT_GUILD
    private static final Pattern MSG_LEFT_GUILD = Pattern.compile("^§3You have left §b[a-zA-Z\\s]+§3!$");

    // Test in GuildModel_MSG_JOINED_GUILD
    private static final Pattern MSG_JOINED_GUILD = Pattern.compile("^§3You have joined §b([a-zA-Z\\s]+)§3!$");

    // Test in GuildModel_MSG_RANK_CHANGED
    private static final Pattern MSG_RANK_CHANGED = Pattern.compile(
            "^§3\\[INFO]§b [\\w]{1,16} has set ([\\w]{1,16})'s? guild rank from (?:Recruit|Recruiter|Captain|Strategist|Chief|Owner) to (Recruit|Recruiter|Captain|Strategist|Chief|Owner)$");

    // Test in GuildModel_MSG_OBJECTIVE_COMPLETED
    private static final Pattern MSG_OBJECTIVE_COMPLETED =
            Pattern.compile("^§3\\[INFO\\]§b (?<player>[\\w]{1,16}) has finished their weekly objective\\.$");

    // Test in GuildModel_MSG_NEW_OBJECTIVES
    private static final Pattern MSG_NEW_OBJECTIVES =
            Pattern.compile("^§3\\[INFO\\]§b New Weekly Guild Objectives are being assigned\\.$");

    // Test in GuildModel_LEVEL_MATCHER
    private static final Pattern LEVEL_MATCHER = Pattern.compile("^§b§l[a-zA-Z\\s]+§3§l \\[Lv\\. (?<level>\\d+)\\]$");

    // Test in GuildModel_LEVEL_PROGRESS_MATCHER
    private static final Pattern LEVEL_PROGRESS_MATCHER =
            Pattern.compile("^§f(?<current>[\\d\\,]+)§7/(?<required>[\\d\\,]+) XP$");

    // Test in GuildModel_OBJECTIVES_COMPLETED_PATTERN
    private static final Pattern OBJECTIVES_COMPLETED_PATTERN =
            Pattern.compile("^§6Current Guild Goal: §f(?<completed>\\d+)§7/(?<goal>\\d+)$");

    // Test in GuildModel_OBJECTIVE_STREAK_PATTERN
    private static final Pattern OBJECTIVE_STREAK_PATTERN = Pattern.compile("^§a- §7Streak: §f(?<streak>\\d+)$");

    // Test in GuildModel_TRIBUTE_PATTERN
    private static final Pattern TRIBUTE_PATTERN = Pattern.compile(
            "^§[abef6](?<symbol>[ⒷⒸⓀⒿ]?) ?(?<amount>[+-]\\d+) (Ore|Wood|Fish|Crops?|Emeralds?) per Hour$");

    private static final int MEMBERS_SLOT = 0;
    private static final int OBJECTIVES_SLOT = 13;
    public static final int DIPLOMACY_MENU_SLOT = 26;
    private static final List<Integer> DIPLOMAC_SLOTS = List.of(2, 3, 4, 5, 6, 7, 8);

    private static final List<Integer> OBJECTIVE_GOALS = List.of(5, 15, 30);

    private Map<String, GuildProfile> guildProfileMap = new HashMap<>();
    private final Map<String, Map<GuildResource, Integer>> guildDiplomacyMap = new HashMap<>();

    private String guildName = "";
    private GuildRank guildRank;
    private int guildLevel = -1;
    private CappedValue guildLevelProgress = CappedValue.EMPTY;
    private CappedValue objectivesCompletedProgress = CappedValue.EMPTY;
    private int objectiveStreak = 0;

    public GuildModel() {
        super(List.of());

        Handlers.Label.registerParser(new GuildSeasonLeaderboardHeaderLabelParser());
        Handlers.Label.registerParser(new GuildSeasonLeaderboardLabelParser());

        loadGuildList();
    }

    // This needs to run before any chat modifications (eg. chat mentions, filter, etc)
    // Side note; it is currently impossible to detect when we get kicked as there are no messages sent at all
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatMessage(ChatMessageReceivedEvent e) {
        StyledText message = StyledText.fromComponent(e.getMessage());

        if (message.matches(MSG_LEFT_GUILD)) {
            guildName = "";
            guildRank = null;
            guildLevel = -1;
            guildLevelProgress = CappedValue.EMPTY;
            objectivesCompletedProgress = CappedValue.EMPTY;
            objectiveStreak = 0;
            WynntilsMod.info("User left guild");
            return;
        }

        Matcher joinedGuildMatcher = message.getMatcher(MSG_JOINED_GUILD);
        if (joinedGuildMatcher.matches()) {
            guildName = joinedGuildMatcher.group(1);
            guildRank = GuildRank.RECRUIT;
            WynntilsMod.info("User joined guild " + guildName + " as a " + guildRank);
            return;
        }

        Matcher rankChangedMatcher = message.getMatcher(MSG_RANK_CHANGED);
        if (rankChangedMatcher.matches()) {
            if (!rankChangedMatcher.group(1).equals(McUtils.playerName())) return;
            guildRank = GuildRank.valueOf(rankChangedMatcher.group(2).toUpperCase(Locale.ROOT));
            WynntilsMod.info("User's guild rank changed to " + guildRank);
            return;
        }

        // Handle completed objective
        Matcher objectiveCompletedMatcher = message.getMatcher(MSG_OBJECTIVE_COMPLETED);
        if (objectiveCompletedMatcher.matches()) {
            int currentGoal = objectivesCompletedProgress.max();
            int completed = objectivesCompletedProgress.current() + 1;
            // Get next goal
            for (int goal : OBJECTIVE_GOALS) {
                if (completed >= currentGoal) {
                    currentGoal = goal;
                } else {
                    break;
                }
            }
            objectivesCompletedProgress = new CappedValue(completed, currentGoal);

            // Update streak
            if (objectiveCompletedMatcher.group("player").equals(McUtils.playerName())) {
                objectiveStreak++;
            }
            return;
        }

        Matcher newObjectivesMatcher = message.getMatcher(MSG_NEW_OBJECTIVES);
        if (newObjectivesMatcher.matches()) {
            objectivesCompletedProgress = new CappedValue(0, OBJECTIVE_GOALS.get(0));
            return;
        }
    }

    public void parseGuildInfoFromGuildMenu(ItemStack guildInfoItem) {
        List<StyledText> lore = LoreUtils.getLore(guildInfoItem);

        for (StyledText line : lore) {
            Matcher guildNameMatcher = line.getMatcher(GUILD_NAME_MATCHER);
            if (guildNameMatcher.matches()) {
                guildName = guildNameMatcher.group("name");
                continue;
            }

            Matcher rankMatcher = line.getMatcher(GUILD_RANK_MATCHER);
            if (rankMatcher.matches()) {
                guildRank = GuildRank.valueOf(rankMatcher.group(1).toUpperCase(Locale.ROOT));
                break;
            }
        }

        WynntilsMod.info("Successfully parsed guild name and rank, " + guildRank + " of " + guildName);
    }

    public void parseGuildContainer(ContainerContent container) {
        ItemStack membersItem = container.items().get(MEMBERS_SLOT);
        ItemStack objectivesItem = container.items().get(OBJECTIVES_SLOT);

        Matcher levelMatcher = StyledText.fromComponent(membersItem.getHoverName())
                .getNormalized()
                .getMatcher(LEVEL_MATCHER);
        if (!levelMatcher.matches()) {
            WynntilsMod.warn("Could not parse guild level from item: " + LoreUtils.getLore(membersItem));
            return;
        }
        guildLevel = Integer.parseInt(levelMatcher.group("level"));

        Matcher levelProgressMatcher = LoreUtils.matchLoreLine(membersItem, 0, LEVEL_PROGRESS_MATCHER);
        if (!levelMatcher.matches()) {
            WynntilsMod.warn("Could not parse guild level progress from item: " + LoreUtils.getLore(membersItem));
            return;
        }
        long current = Long.parseLong(levelProgressMatcher.group("current").replace(",", ""));
        long required = Long.parseLong(levelProgressMatcher.group("required").replace(",", ""));
        guildLevelProgress = new CappedValue((int) (((double) current / required) * 100d), 100);

        for (StyledText line : LoreUtils.getLore(objectivesItem)) {
            Matcher objectivesCompletedMatcher = line.getMatcher(OBJECTIVES_COMPLETED_PATTERN);
            if (objectivesCompletedMatcher.matches()) {
                objectivesCompletedProgress = new CappedValue(
                        Integer.parseInt(objectivesCompletedMatcher.group("completed")),
                        Integer.parseInt(objectivesCompletedMatcher.group("goal")));
                continue;
            }

            Matcher objectiveStreakMatcher = line.getMatcher(OBJECTIVE_STREAK_PATTERN);
            if (objectiveStreakMatcher.matches()) {
                objectiveStreak = Integer.parseInt(objectiveStreakMatcher.group("streak"));
                break;
            }
        }

        WynntilsMod.info("Successfully parsed guild info for guild " + guildName);
    }

    public void parseDiplomacyContainer(ContainerContent content) {
        for (int slot : DIPLOMAC_SLOTS) {
            ItemStack diplomacyItem = content.items().get(slot);

            Matcher alliedGuildNameMatcher = StyledText.fromComponent(diplomacyItem.getHoverName())
                    .getNormalized()
                    .getMatcher(GUILD_NAME_MATCHER);
            if (!alliedGuildNameMatcher.matches()) {
                WynntilsMod.warn("Could not parse allied guild from item: " + LoreUtils.getLore(diplomacyItem));
                continue;
            }

            Map<GuildResource, Integer> tributesMap = new EnumMap<>(GuildResource.class);

            for (StyledText line : LoreUtils.getLore(diplomacyItem)) {
                Matcher tributeMatcher = line.getMatcher(TRIBUTE_PATTERN);
                if (tributeMatcher.matches()) {
                    tributesMap.put(
                            GuildResource.fromSymbol(tributeMatcher.group("symbol")),
                            Integer.parseInt(tributeMatcher.group("amount")));
                }
            }

            guildDiplomacyMap.put(alliedGuildNameMatcher.group("name"), tributesMap);
        }

        WynntilsMod.info("Successfully parsed tributes for guild " + guildName);
    }

    public String getGuildName() {
        return guildName;
    }

    public GuildRank getGuildRank() {
        return guildRank;
    }

    public int getGuildLevel() {
        return guildLevel;
    }

    public void setGuildLevel(int guildLevel) {
        this.guildLevel = guildLevel;
    }

    public CappedValue getGuildLevelProgress() {
        return guildLevelProgress;
    }

    public void setGuildLevelProgress(CappedValue guildLevelProgress) {
        this.guildLevelProgress = guildLevelProgress;
    }

    public CappedValue getObjectivesCompletedProgress() {
        return objectivesCompletedProgress;
    }

    public int getObjectiveStreak() {
        return objectiveStreak;
    }

    public Optional<GuildProfile> getGuildProfile(String name) {
        return Optional.ofNullable(guildProfileMap.get(name));
    }

    public Set<String> getAllGuilds() {
        return guildProfileMap.keySet();
    }

    public int getRecievedTributesForResource(GuildResource resource) {
        return guildDiplomacyMap.values().stream()
                .mapToInt(tributes -> tributes.getOrDefault(resource, 0))
                .filter(value -> value > 0)
                .sum();
    }

    public int getSentTributesForResource(GuildResource resource) {
        return guildDiplomacyMap.values().stream()
                .mapToInt(tributes -> tributes.getOrDefault(resource, 0))
                .filter(value -> value < 0)
                .map(Math::abs)
                .sum();
    }

    public List<String> getAlliedGuilds() {
        return guildDiplomacyMap.keySet().stream().toList();
    }

    public String getGuildNameFromString(String input) {
        // Check for exact guild name
        if (guildProfileMap.containsKey(input)) {
            return input;
        }

        // Check for case insensitive name
        for (String key : guildProfileMap.keySet()) {
            if (key.equalsIgnoreCase(input)) {
                return key;
            }
        }

        // Check for prefix
        for (GuildProfile profile : guildProfileMap.values()) {
            if (profile.prefix().equals(input)) {
                return profile.name();
            }
        }

        // Check for case insensitive prefix
        for (GuildProfile profile : guildProfileMap.values()) {
            if (profile.prefix().equalsIgnoreCase(input)) {
                return profile.name();
            }
        }

        return input;
    }

    public CompletableFuture<GuildInfo> getGuild(String inputName) {
        CompletableFuture<GuildInfo> future = new CompletableFuture<>();

        String guildToSearch = getGuildNameFromString(inputName);

        ApiResponse apiResponse = Managers.Net.callApi(UrlId.DATA_WYNNCRAFT_GUILD, Map.of("name", guildToSearch));
        apiResponse.handleJsonObject(
                json -> {
                    Type type = new TypeToken<GuildInfo>() {}.getType();

                    future.complete(GUILD_PROFILE_GSON.fromJson(json, type));
                },
                onError -> future.complete(null));

        return future;
    }

    public CustomColor getColor(String guildName) {
        return getGuildProfile(guildName).map(GuildProfile::color).orElse(CustomColor.colorForStringHash(guildName));
    }

    private void loadGuildList() {
        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_GUILD_LIST);
        dl.handleJsonArray(json -> {
            Type type = new TypeToken<List<GuildProfile>>() {}.getType();
            List<GuildProfile> guildProfiles = GUILD_PROFILE_GSON.fromJson(json, type);

            Map<String, GuildProfile> profileMap = guildProfiles.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(GuildProfile::name, guildProfile -> guildProfile));

            guildProfileMap = profileMap;
        });
    }
}
