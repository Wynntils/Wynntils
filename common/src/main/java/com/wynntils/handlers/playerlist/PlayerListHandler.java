/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.playerlist;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.handlers.playerlist.event.PlayerListColumnUpdatedEvent;
import com.wynntils.mc.event.PlayerInfoUpdateEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class PlayerListHandler extends Handler {
    private static final String FRIEND_COLUMN_TITLE = "§a  §lFriends";
    private static final String PARTY_COLUMN_TITLE = "§e  §lParty";
    private static final String GUILD_COLUMN_TITLE = "§b§l  Guild";
    private static final String GLOBAL_COLUMN_TITLE_PREFIX = "§f  §lGlobal";

    private Column recordingColumn = Column.Unknown;

    private List<ClientboundPlayerInfoUpdatePacket.Entry> friends = new ArrayList<>();
    private List<ClientboundPlayerInfoUpdatePacket.Entry> party = new ArrayList<>();
    private List<ClientboundPlayerInfoUpdatePacket.Entry> guild = new ArrayList<>();

    private List<ClientboundPlayerInfoUpdatePacket.Entry> newFriends = new ArrayList<>();
    private List<ClientboundPlayerInfoUpdatePacket.Entry> newParty = new ArrayList<>();
    private List<ClientboundPlayerInfoUpdatePacket.Entry> newGuild = new ArrayList<>();

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) return;

        // Friends and guild don't change often through world swap.
        party.clear();

        recordingColumn = Column.Unknown;
    }

    /**
     * Wynn initially sends a packet with 80 fake players using all player info actions. Later-on, these fake players
     * are updated using upto 80 packets but only with the UPDATE_DISPLAY_NAME action. Thus we will start recording
     * these entries if there is only 1 player in the packet and only the UPDATE_DISPLAY_NAME action is used. We will
     * then record next packets and ignore all empty or null ones.
     * @param event contains information regarding the player list.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerUpdateEvent(PlayerInfoUpdateEvent event) {
        List<ClientboundPlayerInfoUpdatePacket.Entry> entries = event.getEntries();
        if (entries.size() != 1) return;
        if (event.getActions().size() != 1
                || !event.getActions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) return;

        ClientboundPlayerInfoUpdatePacket.Entry entry = entries.getFirst();
        if (entry.displayName().getString().equals(FRIEND_COLUMN_TITLE)) {
            newFriends = new ArrayList<>();
            newParty = new ArrayList<>();
            newGuild = new ArrayList<>();
            recordingColumn = Column.Friends;
            return; // We don't record the title
        } else if (entry.displayName().getString().startsWith(GLOBAL_COLUMN_TITLE_PREFIX)) {
            recordingColumn = Column.Global;
            return;
        } else if (entry.displayName().getString().equals(PARTY_COLUMN_TITLE)) {
            recordingColumn = Column.Party;
            return; // We don't record the title
        } else if (entry.displayName().getString().equals(GUILD_COLUMN_TITLE)) {
            recordingColumn = Column.Guild;
            return; // We don't record the title
        } else if (recordingColumn == Column.Unknown || recordingColumn == Column.Global) {
            return;
        }

        boolean emptyOrOffline = entry.displayName().getString().isEmpty() ||
                entry.displayName().getString().startsWith(ChatFormatting.GRAY.toString());

        if (recordingColumn == Column.Guild) {
            if (!emptyOrOffline) {
                newGuild.add(entry);
            }

            // We have reached the end.
            // 19 since each column is of size 20 including the title.
            if (emptyOrOffline || guild.size() >= 19) {
                recordingColumn = Column.Unknown;

                // Only send the event if the old list was empty and we found something or if something changed.
                if (isListDifferent(friends, newFriends) || friends.removeAll(newFriends)) {
                    WynntilsMod.postEvent(new PlayerListColumnUpdatedEvent.Friends(new ArrayList<>(friends)));
                }
                if  (isListDifferent(party, newParty) || party.removeAll(newParty)) {
                    WynntilsMod.postEvent(new PlayerListColumnUpdatedEvent.Party(newParty));
                }
                if  (isListDifferent(guild, newGuild) || guild.removeAll(newGuild)) {
                    WynntilsMod.postEvent(new PlayerListColumnUpdatedEvent.Guild(newGuild));
                }

                this.friends = newFriends;
                this.party = newParty;
                this.guild = newGuild;
            }
        } else if (emptyOrOffline) {
            return;
        } else if (recordingColumn == Column.Friends) {
            newFriends.add(entry);
        } else if (recordingColumn == Column.Party) {
            newParty.add(entry);
        }
    }

    private boolean isListDifferent(List<ClientboundPlayerInfoUpdatePacket.Entry> list1, List<ClientboundPlayerInfoUpdatePacket.Entry> list2) {
        return list1.size() != list2.size() || !new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    private enum Column {
        Unknown,
        Friends,
        Global,
        Party,
        Guild,
    }
}
