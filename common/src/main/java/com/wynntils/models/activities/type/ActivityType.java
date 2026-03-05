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
    RECOMMENDED("Recommended", "recommended", null, null, null),
    QUEST("Quest", "quests", CustomColor.fromInt(0x29cc96), Texture.QUEST_ICON, "quest"),
    STORYLINE_QUEST("Quest", "quests", CustomColor.fromInt(0x33b33b), Texture.STORYLINE_QUEST_ICON, "story_quest"),
    MINI_QUEST("Mini-Quest", "mini-quests", CustomColor.fromInt(0xb38fad), Texture.MINI_QUEST_ICON, "mini_quest"),
    WORLD_EVENT("World Event", "world events", CustomColor.fromInt(0x00bdbf), Texture.WORLD_EVENT_ICON, "world_event"),
    SECRET_DISCOVERY(
            "Secret Discovery",
            "secret discoveries",
            CustomColor.fromInt(0xa1c3e6),
            Texture.DISCOVERY_ICON,
            "discovery"),
    WORLD_DISCOVERY(
            "World Discovery", "world discoveries", CustomColor.fromInt(0xa1c3e6), Texture.DISCOVERY_ICON, "discovery"),
    TERRITORIAL_DISCOVERY(
            "Territorial Discovery",
            "territorial discoveries",
            CustomColor.fromInt(0xa1c3e6),
            Texture.DISCOVERY_ICON,
            "discovery"),
    CAVE("Cave", "caves", CustomColor.fromInt(0xff8c19), Texture.CAVE, "cave"),
    DUNGEON("Dungeon", "dungeons", CustomColor.fromInt(0xcc6677), Texture.DUNGEON_ENTRANCE, "dungeon"),
    RAID("Raid", "raids", CustomColor.fromInt(0xd6401e), Texture.RAID_ENTRANCE, "raid"),
    BOSS_ALTAR("Boss Altar", "boss altars", CustomColor.fromInt(0xf2d349), Texture.BOSS_ALTAR, "boss_altar"),
    LOOTRUN_CAMP("Lootrun Camp", "lootrun camps", CustomColor.fromInt(0x3399cc), Texture.LOOTRUN_CAMP, "loot_camp");

    private final String displayName;
    private final String groupName;
    private final CustomColor color;
    private final Texture texture;
    private final FontDescription fontIcon;

    ActivityType(String displayName, String groupName, CustomColor color, Texture texture, String fontIcon) {
        this.displayName = displayName;
        this.groupName = groupName;
        this.color = color;
        this.texture = texture;
        this.fontIcon = new FontDescription.AtlasSprite(
                Identifier.withDefaultNamespace("items"),
                Identifier.withDefaultNamespace("wynn/gui/content_book/" + fontIcon + "_active"));
    }
    ;

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
