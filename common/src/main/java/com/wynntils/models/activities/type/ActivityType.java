/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import com.wynntils.core.text.StyledTextPart;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import java.util.Objects;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public enum ActivityType {
    RECOMMENDED("Recommended", "recommended", null, null, null, false),
    QUEST("Quest", "quests", CustomColor.fromInt(0x29cc96), Texture.QUEST_ICON, "quest", false),
    STORYLINE_QUEST(
            "Quest", "quests", CustomColor.fromInt(0x33b33b), Texture.STORYLINE_QUEST_ICON, "story_quest", false),
    MINI_QUEST(
            "Mini-Quest", "mini-quests", CustomColor.fromInt(0xb38fad), Texture.MINI_QUEST_ICON, "mini_quest", false),
    WORLD_EVENT(
            "World Event",
            "world events",
            CustomColor.fromInt(0x00bdbf),
            Texture.WORLD_EVENT_ICON,
            "world_event",
            true),
    SECRET_DISCOVERY(
            "Secret Discovery",
            "secret discoveries",
            CustomColor.fromInt(0xa1c3e6),
            Texture.DISCOVERY_ICON,
            "discovery",
            false),
    WORLD_DISCOVERY(
            "World Discovery",
            "world discoveries",
            CustomColor.fromInt(0xa1c3e6),
            Texture.DISCOVERY_ICON,
            "discovery",
            false),
    TERRITORIAL_DISCOVERY(
            "Territorial Discovery",
            "territorial discoveries",
            CustomColor.fromInt(0xa1c3e6),
            Texture.DISCOVERY_ICON,
            "discovery",
            false),
    CAVE("Cave", "caves", CustomColor.fromInt(0xff8c19), Texture.CAVE, "cave", false),
    DUNGEON("Dungeon", "dungeons", CustomColor.fromInt(0xcc6677), Texture.DUNGEON_ENTRANCE, "dungeon", false),
    RAID("Raid", "raids", CustomColor.fromInt(0xd6401e), Texture.RAID_ENTRANCE, "raid", false),
    BOSS_ALTAR("Boss Altar", "boss altars", CustomColor.fromInt(0xf2d349), Texture.BOSS_ALTAR, "boss_altar", false),
    LOOTRUN_CAMP(
            "Lootrun Camp", "lootrun camps", CustomColor.fromInt(0x3399cc), Texture.LOOTRUN_CAMP, "loot_camp", false),
    MOUNT_ENCLOSURE(
            "Mount Enclosure",
            "mount enclosures",
            CustomColor.fromInt(0x8f663d),
            Texture.MOUNT_ENCLOSURE,
            "mount_enclosure",
            false),
    TIME_TRIAL("Time Trial", "time trials", CustomColor.fromInt(0x1ebf97), Texture.TIME_TRIAL, "time_trial", true);

    private final String displayName;
    private final String groupName;
    private final CustomColor color;
    private final Texture texture;
    private final FontDescription fontIcon;
    private final boolean supportsFastTravel;

    ActivityType(
            String displayName,
            String groupName,
            CustomColor color,
            Texture texture,
            String fontIcon,
            boolean supportsFastTravel) {
        this.displayName = displayName;
        this.groupName = groupName;
        this.color = color;
        this.texture = texture;
        this.fontIcon = new FontDescription.AtlasSprite(
                Identifier.withDefaultNamespace("items"),
                Identifier.withDefaultNamespace("wynn/gui/content_book/" + fontIcon + "_active"));
        this.supportsFastTravel = supportsFastTravel;
    }

    public static ActivityType from(CustomColor color, String displayName) {
        for (ActivityType type : values()) {
            if (Objects.equals(type.getColor(), color) && type.getDisplayName().equals(displayName)) {
                return type;
            }
        }

        return null;
    }

    /** This version cannot distinguish between QUEST and STORYLINE_QUEST */
    public static ActivityType from(String displayName) {
        for (ActivityType type : values()) {
            if (type.getDisplayName().equals(displayName)) return type;
        }

        // Time trials are displayed on the scoreboard with this, but just Time Trial in the content book
        if (displayName.equals("Mount Time Trial")) return TIME_TRIAL;

        // FIXME: Workaround to stop crashes
        if (displayName.equals("Discovery")) return WORLD_DISCOVERY;

        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroupName() {
        return groupName;
    }

    public CustomColor getColor() {
        return color;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isQuest() {
        return this == QUEST || this == STORYLINE_QUEST || this == MINI_QUEST;
    }

    public boolean isDiscovery() {
        return this == SECRET_DISCOVERY || this == WORLD_DISCOVERY || this == TERRITORIAL_DISCOVERY;
    }

    public boolean supportsFastTravel() {
        return supportsFastTravel;
    }

    public boolean matchesTracking(ActivityType activityType) {
        // When tracking activities, storyline quests and mini-quests cannot
        // be distinguished from quests
        return switch (this) {
            case STORYLINE_QUEST, MINI_QUEST -> activityType == this || activityType == QUEST;
            default -> activityType == this;
        };
    }

    public StyledTextPart getIcon() {
        return new StyledTextPart("A", Style.EMPTY.withFont(fontIcon), null, null);
    }
}
