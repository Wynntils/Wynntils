/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.worldevents;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.labels.event.LabelsRemovedEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.activities.bossbars.AnnihilationSunBar;
import com.wynntils.models.activities.event.AnnihilationEvent;
import com.wynntils.models.activities.label.WorldEventCountdownInfo;
import com.wynntils.models.activities.label.WorldEventCountdownParser;
import com.wynntils.models.activities.label.WorldEventNameInfo;
import com.wynntils.models.activities.label.WorldEventNameParser;
import com.wynntils.models.activities.type.ActivityDifficulty;
import com.wynntils.models.activities.type.ActivityDistance;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivityLength;
import com.wynntils.models.activities.type.WorldEvent;
import com.wynntils.models.containers.containers.reward.EventContainer;
import com.wynntils.models.containers.event.ValuableFoundEvent;
import com.wynntils.models.items.items.game.CorruptedCacheItem;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.VectorUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Position;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class WorldEventModel extends Model {
    public static final AnnihilationSunBar annihilationSunBar = new AnnihilationSunBar();

    private static final String ANNIHILATION_WORLD_EVENT_NAME = "Prelude to Annihilation";
    private static final Position ANNIHILATION_WORLD_EVENT_LOCATION = new Vec3(315.5, 29.0, -1291.5);
    private static final Integer ANNIHILATION_WORLD_EVENT_RADIUS = 50;

    @Persisted
    public final Storage<Integer> dryAnnihilations = new Storage<>(0);

    // Handles whether you are in the world event or not
    private static final Pattern IN_RADIUS_PATTERN =
            Pattern.compile("§#00bdbfff(\uE00D\uE002|\uE001) You are now within the event radius");
    private static final Pattern OUT_OF_RADIUS_PATTERN =
            Pattern.compile("§#00bdbfff(\uE00D\uE002|\uE001) You are no longer within the event radius");
    private static final Pattern DID_NOT_ENTER_PATTERN =
            Pattern.compile("§#00bdbfff(\uE00D\uE002|\uE001) You did not enter the event radius in time");

    // Handles parsing timers from chat
    private static final Pattern ANNIHILATION_TIMER_PATTERN = Pattern.compile(
            "§#00bdbfff(\uE00D\uE002|\uE001) §cPrepare to defend the province at the Corruption Portal in(?: (?<hour>\\d+)h)?(?: (?<minute>\\d+)m)?(?: (?<second>\\d+)s)?!");
    private static final Pattern WORLD_EVENT_PATTERN = Pattern.compile(
            "§#00bdbfff(\uE00D\uE002|\uE001) (?<worldEventName>.+?) World Event starts in(?: (?<hour>\\d+)h)?(?: (?<minute>\\d+)m)?(?: (?<second>\\d+)s)?! §7\\(\\d+ blocks away\\) §d§nClick to track");

    // Handles completion/failure
    private static final Pattern WORLD_EVENT_COMPLETE_PATTERN = Pattern.compile("§#00bdbfff\uE001 §fEvent Completed");
    private static final Pattern WORLD_EVENT_FAIL_PATTERN = Pattern.compile("§#00bdbfff\uE001 §fEvent Failed");

    private final Map<String, WorldEvent> activeWorldEvents = new HashMap<>();

    private boolean inWorldEventRadius = false;
    private String nearestWorldEventName = "";
    private Time nearestWorldEventStartTime = Time.NONE;
    private WorldEvent currentWorldEvent = null;
    private WorldEvent nearestWorldEvent = null;

    private boolean nextEventRewardIsAnnihilation = false;
    private int nextExpectedRewardContainerId = -2;

    public WorldEventModel() {
        super(List.of());

        Handlers.BossBar.registerBar(annihilationSunBar);

        Handlers.Label.registerParser(new WorldEventCountdownParser());
        Handlers.Label.registerParser(new WorldEventNameParser());
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof WorldEventCountdownInfo labelInfo) {
            if (nearestWorldEventStartTime != Time.NONE) return;

            nearestWorldEventStartTime = labelInfo.getStartTime();

            if (currentWorldEvent != null) {
                currentWorldEvent.setStartTime(nearestWorldEventStartTime);
            }

            if (nearestWorldEvent != null) {
                nearestWorldEvent.setStartTime(nearestWorldEventStartTime);
            } else if (!nearestWorldEventName.isEmpty()) {
                nearestWorldEvent = new WorldEvent(nearestWorldEventName, nearestWorldEventStartTime);
                activeWorldEvents.put(nearestWorldEventName, nearestWorldEvent);
            }
        } else if (event.getLabelInfo() instanceof WorldEventNameInfo labelInfo) {
            if (currentWorldEvent != null) return;
            if (nearestWorldEventStartTime == Time.NONE) {
                nearestWorldEventName = labelInfo.getWorldEventName();
                return;
            }

            nearestWorldEvent = new WorldEvent(labelInfo.getWorldEventName(), nearestWorldEventStartTime);

            activeWorldEvents.put(labelInfo.getWorldEventName(), nearestWorldEvent);
        }
    }

    @SubscribeEvent
    public void onLabelsRemoved(LabelsRemovedEvent event) {
        Optional<LabelInfo> labelOpt = event.getRemovedLabels().stream()
                .filter(labelInfo -> labelInfo instanceof WorldEventNameInfo)
                .findAny();

        if (labelOpt.isPresent()) {
            // Special case for world events with a large radius like Annihilation
            if (!inWorldEventRadius) {
                exitWorldEvent();
            }
        }
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        StyledText styledText = StyledTextUtils.unwrap(event.getMessage()).stripAlignment();

        if (styledText.matches(IN_RADIUS_PATTERN)) {
            inWorldEventRadius = true;

            if (nearestWorldEvent == null) {
                if (VectorUtils.distanceIgnoringY(McUtils.player().position(), ANNIHILATION_WORLD_EVENT_LOCATION)
                        < ANNIHILATION_WORLD_EVENT_RADIUS) {
                    nearestWorldEvent = new WorldEvent(ANNIHILATION_WORLD_EVENT_NAME, nearestWorldEventStartTime);

                    if (activeWorldEvents.containsKey(nearestWorldEvent.getName())) {
                        nearestWorldEvent.setStartTime(activeWorldEvents
                                .get(nearestWorldEvent.getName())
                                .getStartTime());
                    } else {
                        activeWorldEvents.put(nearestWorldEvent.getName(), nearestWorldEvent);
                    }
                }
            }

            currentWorldEvent = nearestWorldEvent;
            return;
        } else if (styledText.matches(OUT_OF_RADIUS_PATTERN)) {
            inWorldEventRadius = false;

            nearestWorldEvent = currentWorldEvent;
            exitWorldEvent();
            return;
        } else if (styledText.matches(DID_NOT_ENTER_PATTERN) && nearestWorldEvent != null) {
            activeWorldEvents.remove(nearestWorldEvent.getName());
            nearestWorldEvent = null;
            nearestWorldEventStartTime = Time.NONE;
            return;
        } else if (styledText.matches(WORLD_EVENT_COMPLETE_PATTERN)) {
            if (currentWorldEvent == null) {
                WynntilsMod.warn("Completed a world event but current world event was unknown");
                return;
            }

            if (currentWorldEvent.getName().equals(ANNIHILATION_WORLD_EVENT_NAME)) {
                nextEventRewardIsAnnihilation = true;
                WynntilsMod.postEvent(new AnnihilationEvent.Completed());
            }

            currentWorldEvent = null;

            return;
        } else if (styledText.matches(WORLD_EVENT_FAIL_PATTERN)) {
            currentWorldEvent = null;

            if (currentWorldEvent.getName().equals(ANNIHILATION_WORLD_EVENT_NAME)) {
                WynntilsMod.postEvent(new AnnihilationEvent.Failed());
            }
            return;
        }

        Matcher matcher = styledText.getMatcher(ANNIHILATION_TIMER_PATTERN);

        if (!activeWorldEvents.containsKey(ANNIHILATION_WORLD_EVENT_NAME) && matcher.matches()) {
            Time anniStartTime =
                    parseWorldEventStartTime(matcher.group("hour"), matcher.group("minute"), matcher.group("second"));

            activeWorldEvents.put(
                    ANNIHILATION_WORLD_EVENT_NAME, new WorldEvent(ANNIHILATION_WORLD_EVENT_NAME, anniStartTime));
            return;
        }

        matcher = styledText.getMatcher(WORLD_EVENT_PATTERN);

        if (matcher.matches()) {
            String worldEventName = matcher.group("worldEventName");
            Time startTime =
                    parseWorldEventStartTime(matcher.group("hour"), matcher.group("minute"), matcher.group("second"));

            activeWorldEvents.put(worldEventName, new WorldEvent(worldEventName, startTime));
            return;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        nearestWorldEvent = null;
        nearestWorldEventStartTime = Time.NONE;
        exitWorldEvent();
    }

    @SubscribeEvent
    public void onTickAlways(TickEvent e) {
        if (McUtils.player().tickCount % 20 != 0) return;

        activeWorldEvents
                .entrySet()
                .removeIf(entry -> entry.getValue().getStartTime().getOffset(Time.now()) >= 0);
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent.Pre e) {
        if (Models.Container.getCurrentContainer() instanceof EventContainer eventContainer) {
            nextExpectedRewardContainerId = eventContainer.getContainerId();
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent.Post e) {
        if (nextEventRewardIsAnnihilation) {
            dryAnnihilations.store(dryAnnihilations.get() + 1);
            nextEventRewardIsAnnihilation = false;
        }

        nextExpectedRewardContainerId = -2;
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != nextExpectedRewardContainerId) return;
        if (event.getSlot() >= Models.LootChest.LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        Optional<CorruptedCacheItem> cacheItem = Models.Item.asWynnItem(itemStack, CorruptedCacheItem.class);
        if (cacheItem.isPresent()) {
            WynntilsMod.postEvent(new ValuableFoundEvent(itemStack, ValuableFoundEvent.ItemSource.WORLD_EVENT));

            dryAnnihilations.store(0);
            // You can only get 1 cache so we can stop checking if we find one
            nextExpectedRewardContainerId = -2;
            nextEventRewardIsAnnihilation = false;
            return;
        }
    }

    public WorldEvent getCurrentWorldEvent() {
        return currentWorldEvent;
    }

    public WorldEvent getWorldEvent(String worldEventName) {
        return activeWorldEvents.get(worldEventName);
    }

    public Time parseWorldEventStartTime(String hour, String minute, String second) {
        int hours = hour != null ? Integer.parseInt(hour) : 0;
        int minutes = minute != null ? Integer.parseInt(minute) : 0;
        int seconds = second != null ? Integer.parseInt(second) : 0;

        long currentTime = System.currentTimeMillis();
        long countDown = ((hours * 60L + minutes) * 60L + seconds) * 1000L;
        long rawStartTime = currentTime + countDown;

        // World Events always start on exact minutes so we need to round up to the next minute
        long remainder = rawStartTime % 60000L;
        long startTime = remainder == 0 ? rawStartTime : rawStartTime + (60000L - remainder);

        return Time.of(startTime);
    }

    private void exitWorldEvent() {
        nearestWorldEvent = currentWorldEvent;
        nearestWorldEventStartTime = Time.NONE;
        currentWorldEvent = null;
    }

    private WorldEventInfo getWorldEventInfoFromActivity(ActivityInfo activity) {
        return new WorldEventInfo(
                activity.name(),
                activity.specialInfo().orElse(""),
                activity.description().orElse(StyledText.EMPTY).getString(),
                activity.status(),
                activity.requirements().level().key(),
                activity.distance().orElse(ActivityDistance.NEAR),
                activity.length().orElse(ActivityLength.SHORT),
                activity.difficulty().orElse(ActivityDifficulty.EASY),
                activity.rewards());
    }
}
