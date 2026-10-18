/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.character.event.CharacterIdSetEvent;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.models.timetrials.actionbar.matchers.TimeTrialCheckpointTimeSegmentMatcher;
import com.wynntils.models.timetrials.actionbar.matchers.TimeTrialCheckpointsSegmentMatcher;
import com.wynntils.models.timetrials.actionbar.matchers.TimeTrialElapsedTimeSegmentMatcher;
import com.wynntils.models.timetrials.actionbar.segments.TimeTrialCheckpointTimeSegment;
import com.wynntils.models.timetrials.actionbar.segments.TimeTrialCheckpointsSegment;
import com.wynntils.models.timetrials.actionbar.segments.TimeTrialElapsedTimeSegment;
import com.wynntils.models.timetrials.event.TimeTrialEvent;
import com.wynntils.models.timetrials.label.TimeTrialLabelInfo;
import com.wynntils.models.timetrials.label.TimeTrialLabelParser;
import com.wynntils.models.timetrials.type.TimeTrialInfo;
import com.wynntils.models.timetrials.type.TimeTrialState;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public final class TimeTrialModel extends Model {
    private static final Pattern MOUNT_READY_PATTERN = Pattern.compile("§a§lMount Ready");
    private static final Pattern GO_PATTERN = Pattern.compile("§a§lGO!");
    private static final Pattern RACE_RESUMED_PATTERN = Pattern.compile("§6§lRace Resumed");
    private static final Pattern TRIAL_CLEARED_PATTERN = Pattern.compile("§a§lTime Trial Cleared!");
    private static final Pattern TRIAL_FAILED_PATTERN = Pattern.compile("§4§lTime Trial Failed");

    private final Set<Class<? extends ActionBarSegment>> hiddenSegments = new HashSet<>();

    @Persisted
    private final Storage<Map<String, TimeTrialInfo>> currentTimeTrials = new Storage<>(new TreeMap<>());

    private boolean handleResume = false;
    private TimeTrialState state = TimeTrialState.NOT_STARTED;
    private TimeTrialInfo currentTimeTrial = null;
    private long previousCheckpointTime = -1;

    private String nearestTrialName;
    private MountType nearestTrialMountType;

    public TimeTrialModel() {
        super(List.of());

        Handlers.ActionBar.registerSegment(new TimeTrialElapsedTimeSegmentMatcher());
        Handlers.ActionBar.registerSegment(new TimeTrialCheckpointsSegmentMatcher());
        Handlers.ActionBar.registerSegment(new TimeTrialCheckpointTimeSegmentMatcher());
        Handlers.Label.registerParser(new TimeTrialLabelParser());
    }

    @SubscribeEvent
    public void onTitle(TitleSetTextEvent event) {
        StyledText styledText = StyledText.fromComponent(event.getComponent());

        TimeTrialEvent timeTrialEvent = null;

        if (styledText.matches(MOUNT_READY_PATTERN)) {
            state = TimeTrialState.READY;
            timeTrialEvent = new TimeTrialEvent.Ready();

            if (nearestTrialName != null && nearestTrialMountType != null) {
                currentTimeTrial = new TimeTrialInfo(nearestTrialName, nearestTrialMountType);
                updateCurrentTimeTrial();
            } else {
                WynntilsMod.warn("Started time trial with unknown trial name or mount type at "
                        + McUtils.player().position());
            }
        } else if (styledText.matches(GO_PATTERN)) {
            state = TimeTrialState.IN_PROGRESS;
            timeTrialEvent = new TimeTrialEvent.Started();
        } else if (styledText.matches(TRIAL_CLEARED_PATTERN)) {
            state = TimeTrialState.NOT_STARTED;
            removeCurrentTimeTrial();
            timeTrialEvent = new TimeTrialEvent.Cleared();
        } else if (styledText.matches(TRIAL_FAILED_PATTERN)) {
            state = TimeTrialState.NOT_STARTED;
            removeCurrentTimeTrial();
            timeTrialEvent = new TimeTrialEvent.Failed();
        }

        if (timeTrialEvent != null) {
            WynntilsMod.postEvent(timeTrialEvent);
            return;
        }

        if (styledText.matches(RACE_RESUMED_PATTERN)) {
            if (Models.Character.getId().equals("-")) {
                handleResume = true;
                return;
            } else {
                restoreTimeTrial();
            }
        }
    }

    @SubscribeEvent
    public void onCharacterIdSet(CharacterIdSetEvent event) {
        if (!handleResume) return;

        handleResume = false;
        restoreTimeTrial();
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof TimeTrialLabelInfo labelInfo) {
            nearestTrialName = labelInfo.getTrialName();
            nearestTrialMountType = labelInfo.getAvailableMountType();
        }
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresent(TimeTrialCheckpointsSegment.class, this::updateCheckpoints);
        event.runIfPresent(TimeTrialElapsedTimeSegment.class, this::updateElapsedTime);
        event.runIfPresentOrElse(
                TimeTrialCheckpointTimeSegment.class, this::updateCheckpointTime, () -> previousCheckpointTime = -1);
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        hiddenSegments.forEach(segment -> event.setSegmentEnabled(segment, false));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getOldState() == WorldState.WORLD) {
            state = TimeTrialState.NOT_STARTED;
        }
    }

    public TimeTrialState getState() {
        return state;
    }

    public String getCurrentTrialName() {
        if (currentTimeTrial == null) return "";

        return currentTimeTrial.getTimeTrialName();
    }

    public MountType getCurrentTrialMountType() {
        if (currentTimeTrial == null) return null;

        return currentTimeTrial.getMountType();
    }

    public TimeTrialInfo getCurrentTimeTrial() {
        return currentTimeTrial;
    }

    public long getPreviousCheckpointTime() {
        return previousCheckpointTime;
    }

    public void setHideTrialStats(boolean shouldHide) {
        if (shouldHide) {
            hiddenSegments.add(TimeTrialCheckpointsSegment.class);
            hiddenSegments.add(TimeTrialElapsedTimeSegment.class);
            hiddenSegments.add(TimeTrialCheckpointTimeSegment.class);
        } else {
            hiddenSegments.remove(TimeTrialCheckpointsSegment.class);
            hiddenSegments.remove(TimeTrialElapsedTimeSegment.class);
            hiddenSegments.remove(TimeTrialCheckpointTimeSegment.class);
        }
    }

    private void updateCurrentTimeTrial() {
        Map<String, TimeTrialInfo> current = currentTimeTrials.get();
        current.put(Models.Character.getId(), currentTimeTrial);

        currentTimeTrials.store(current);
        currentTimeTrials.touched();
    }

    private void removeCurrentTimeTrial() {
        Map<String, TimeTrialInfo> current = currentTimeTrials.get();
        current.remove(Models.Character.getId());

        currentTimeTrials.store(current);
        currentTimeTrials.touched();
    }

    private void updateCheckpoints(TimeTrialCheckpointsSegment checkpointsSegment) {
        if (currentTimeTrial == null) return;

        CappedValue checkpoints = checkpointsSegment.getCheckpoints();

        if (currentTimeTrial.getCheckpoints().equals(checkpoints)) return;

        currentTimeTrial.updateCheckpoints(checkpoints);
        updateCurrentTimeTrial();
    }

    private void updateElapsedTime(TimeTrialElapsedTimeSegment elapsedTimeSegment) {
        if (currentTimeTrial == null) return;

        long elapsedTime = elapsedTimeSegment.getElapsedTime();

        currentTimeTrial.updateElapsedTime(elapsedTime);
        // Don't write to storage as this happens too often, just let the next checkpoint change do it
    }

    private void updateCheckpointTime(TimeTrialCheckpointTimeSegment checkpointTimeSegment) {
        if (currentTimeTrial == null) return;

        previousCheckpointTime = checkpointTimeSegment.getCheckpointTime();
    }

    private void restoreTimeTrial() {
        state = TimeTrialState.IN_PROGRESS;
        currentTimeTrial = currentTimeTrials.get().get(Models.Character.getId());

        WynntilsMod.postEvent(new TimeTrialEvent.Resumed());
    }
}
