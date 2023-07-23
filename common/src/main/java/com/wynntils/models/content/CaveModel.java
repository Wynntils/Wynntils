/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.content;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.content.event.ContentUpdatedEvent;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentSortOrder;
import com.wynntils.models.content.type.ContentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CaveModel extends Model {
    private List<CaveInfo> caves = new ArrayList<>();
    private List<StyledText> caveProgress = List.of();

    public CaveModel(ContentModel contentModel) {
        super(List.of(contentModel));
    }

    public void reloadCaves() {
        WynntilsMod.info("Requesting rescan of caves in Content Book");
        Models.Content.scanContentBook(ContentType.CAVE, this::updateCavesFromQuery);
    }

    private void updateCavesFromQuery(List<ContentInfo> newContent, List<StyledText> progress) {
        List<CaveInfo> newCaves = new ArrayList<>();

        for (ContentInfo content : newContent) {
            if (content.type() != ContentType.CAVE) {
                WynntilsMod.warn("Incorrect cave content type recieved: " + content);
                continue;
            }
            CaveInfo caveInfo = getCaveInfoFromContent(content);
            newCaves.add(caveInfo);
        }
        caves = newCaves;
        caveProgress = progress;
        WynntilsMod.postEvent(new ContentUpdatedEvent(ContentType.CAVE));
        WynntilsMod.info("Updated caves from query, got " + caves.size() + " caves.");
    }

    public Optional<CaveInfo> getCaveInfoFromName(String name) {
        return caves.stream().filter(cave -> cave.getName().equals(name)).findFirst();
    }

    public List<CaveInfo> getSortedCaves(ContentSortOrder sortOrder) {
        return sortCaveInfoList(sortOrder, caves);
    }

    private List<CaveInfo> sortCaveInfoList(ContentSortOrder sortOrder, List<CaveInfo> caveList) {
        // All caves are always sorted by status (available then unavailable), and then
        // the given sort order, and finally a third way if the given sort order is equal.

        CaveInfo trackedCaveInfo = Models.Content.getTrackedCaveInfo();
        String trackedCaveName = trackedCaveInfo != null ? trackedCaveInfo.getName() : "";
        Comparator<CaveInfo> baseComparator =
                Comparator.comparing(caveInfo -> !caveInfo.getName().equals(trackedCaveName));
        return switch (sortOrder) {
            case LEVEL -> caveList.stream()
                    .sorted(baseComparator
                            .thenComparing(CaveInfo::getStatus)
                            .thenComparing(CaveInfo::getRecommendedLevel)
                            .thenComparing(CaveInfo::getName))
                    .toList();
            case DISTANCE -> caveList.stream()
                    .sorted(baseComparator
                            .thenComparing(CaveInfo::getStatus)
                            .thenComparing(CaveInfo::getDistance)
                            .thenComparing(CaveInfo::getName))
                    .toList();
            case ALPHABETIC -> caveList.stream()
                    .sorted(baseComparator
                            .thenComparing(CaveInfo::getStatus)
                            .thenComparing(CaveInfo::getName)
                            .thenComparing(CaveInfo::getRecommendedLevel))
                    .toList();
        };
    }

    public List<StyledText> getCaveProgress() {
        return Collections.unmodifiableList(caveProgress);
    }

    private CaveInfo getCaveInfoFromContent(ContentInfo content) {
        return new CaveInfo(
                content.name(),
                content.status(),
                content.description().orElse(StyledText.EMPTY).getString(),
                content.requirements().level().key(),
                content.distance().get(),
                content.length().get(),
                content.difficulty().get(),
                content.rewards());
    }
}
