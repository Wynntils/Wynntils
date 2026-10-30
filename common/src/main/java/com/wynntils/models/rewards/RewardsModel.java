/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.items.annotators.ItemPageAnnotator;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.AmplifierInfo;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.models.wynnitem.parsing.WynnItemStatsParseResult;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

public final class RewardsModel extends Model {
    private final TomeInfoRegistry tomeInfoRegistry = new TomeInfoRegistry();
    private final CharmInfoRegistry charmInfoRegistry = new CharmInfoRegistry();

    private List<AmplifierInfo> allAmplifierInfo;
    private List<RuneType> allRuneInfo;
    private List<WardType> allWardInfo;

    public RewardsModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        tomeInfoRegistry.registerDownloads(registry);
        charmInfoRegistry.registerDownloads(registry);

        allAmplifierInfo = buildAmplifierInfo();
        allRuneInfo = buildRuneInfo();
        allWardInfo = buildWardInfo();
    }

    public CharmInfo getCharmInfoFromDisplayName(String name) {
        return charmInfoRegistry.getFromDisplayName(name);
    }

    public TomeInfo getTomeInfoFromDisplayName(String name) {
        return tomeInfoRegistry.getFromDisplayName(name);
    }

    public Stream<CharmInfo> getAllCharmInfos() {
        return charmInfoRegistry.getAllCharmInfos();
    }

    public Stream<TomeInfo> getAllTomeInfos() {
        return tomeInfoRegistry.getAllTomeInfos();
    }

    public List<AmplifierInfo> getAllAmplifierInfo() {
        return allAmplifierInfo;
    }

    public List<RuneType> getAllRuneInfo() {
        return allRuneInfo;
    }

    public List<WardType> getAllWardInfo() {
        return allWardInfo;
    }

    public List<ItemObtainInfo> getTomeObtainInfo(TomeInfo tomeInfo) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>(tomeInfo.metaInfo().obtainInfo());

        // If the API gave no info, then use the crowd sourced info
        if (obtainInfo.size() == 1 && obtainInfo.getFirst().equals(ItemObtainInfo.UNKNOWN)) {
            obtainInfo.clear();
        }

        obtainInfo.addAll(Models.WynnItem.getObtainInfo(tomeInfo.name()));
        return obtainInfo;
    }

    public List<ItemObtainInfo> getCharmObtainInfo(CharmInfo charmInfo) {
        List<ItemObtainInfo> obtainInfo = new ArrayList<>(charmInfo.metaInfo().obtainInfo());

        // If the API gave no info, then use the crowd sourced info
        if (obtainInfo.size() == 1 && obtainInfo.getFirst().equals(ItemObtainInfo.UNKNOWN)) {
            obtainInfo.clear();
        }

        obtainInfo.addAll(Models.WynnItem.getObtainInfo(charmInfo.name()));
        return obtainInfo;
    }

    public ItemAnnotation fromCharmItemStack(
            ItemStack itemStack, StyledText name, String displayName, String type, boolean isUnidentified) {
        CharmInfo charmInfo = charmInfoRegistry.getFromDisplayName(displayName);
        if (charmInfo == null) {
            WynntilsMod.warn("Could not find charm info for " + displayName);
            return null;
        }

        CharmItem item = new CharmItem(charmInfo, isUnidentified);
        ItemPageAnnotator.update(item, itemStack);
        return item;
    }

    public TomeItem fromTomeItemStack(ItemStack itemStack, StyledText name, String tomeName, boolean isUnidentified) {
        TomeInfo tomeInfo = tomeInfoRegistry.getFromDisplayName(tomeName);
        if (tomeInfo == null) {
            WynntilsMod.warn("Could not find tome info for " + tomeName + " (Originally "
                    + StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting() + ")");
            return null;
        }

        if (isUnidentified) {
            return new TomeItem(tomeInfo, null);
        }

        WynnItemStatsParseResult result =
                WynnItemParser.parseItemStackStatsPage(itemStack, tomeInfo.getVariableStatsMap());
        if (result.tier() != tomeInfo.tier()) {
            WynntilsMod.warn("Tier for " + tomeInfo.name() + " is reported as " + result.tier());
        }

        return new TomeItem(
                tomeInfo,
                TomeInstance.create(
                        result.rerolls(), tomeInfo, result.identifications(), result.gearInstanceRequirements()));
    }

    private List<AmplifierInfo> buildAmplifierInfo() {
        return List.of(
                new AmplifierInfo(1, 5), new AmplifierInfo(2, 10), new AmplifierInfo(3, 15), new AmplifierInfo(4, 20));
    }

    private List<RuneType> buildRuneInfo() {
        return List.of(RuneType.values());
    }

    private List<WardType> buildWardInfo() {
        return List.of(WardType.values());
    }
}
