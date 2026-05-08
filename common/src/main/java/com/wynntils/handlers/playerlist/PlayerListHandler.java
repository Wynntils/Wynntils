package com.wynntils.handlers.playerlist;

import com.wynntils.core.components.Handler;
import com.wynntils.mc.event.PlayerInfoUpdateEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

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
        if (event.getActions().size() != 1 || !event.getActions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) return;

        ClientboundPlayerInfoUpdatePacket.Entry entry = entries.getFirst();
        System.out.println(entry.displayName().getString().equals(FRIEND_COLUMN_TITLE));
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

        if (recordingColumn == Column.Guild) {
            if (!entry.displayName().getString().isEmpty()) {
                newGuild.add(entry);
            }

            // We have reached the end.
            // 19 since each column is of size 20 including the title.
            if (entry.displayName().getString().isEmpty() || guild.size() >= 19){
                recordingColumn = Column.Unknown;
                friends = newFriends;
                party = newParty;
                guild = newGuild;
            }
        } else if (entry.displayName().getString().isEmpty()) {
            return;
        } else if (recordingColumn == Column.Friends) {
            newFriends.add(entry);
        } else if (recordingColumn == Column.Party) {
            newParty.add(entry);
        }
    }

    private enum Column {
        Unknown,
        Friends,
        Global,
        Party,
        Guild,
    }
}
