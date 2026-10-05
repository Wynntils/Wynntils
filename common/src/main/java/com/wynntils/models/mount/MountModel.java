/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.ArmSwingEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.SetLocalPlayerVehicleEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.containers.containers.MountFeederContainer;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.actionbar.matchers.MountEnergySegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountEnergySegment;
import com.wynntils.models.mount.event.MountEvent;
import com.wynntils.models.mount.label.MountEnclosureLabelInfo;
import com.wynntils.models.mount.label.MountEnclosureLabelParser;
import com.wynntils.models.mount.type.ColorType;
import com.wynntils.models.mount.type.Mount;
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.mount.type.MountColorInfo;
import com.wynntils.models.mount.type.MountColorType;
import com.wynntils.models.mount.type.MountEnclosure;
import com.wynntils.models.mount.type.MountEnclosureInfo;
import com.wynntils.models.mount.type.MountFood;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import com.wynntils.utils.type.CappedValue;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.wynntils.utils.type.Time;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

public final class MountModel extends Model {
    // How long we wait before assuming mount failure
    private static final int MOUNT_TIME_TICKS = 10;
    private Map<String, MountColorInfo> mountColors = new HashMap<>();
    // Parsed from the UI element so is not as accurate as the item tooltip
    private CappedValue currentMountEnergy = CappedValue.EMPTY;
    private boolean hideMountEnergy = false;
    private int summonTick = -1;
    private int dismountTick = -1;
    private MountType expectedMountType = null;
    private Optional<MountType> currentMountType = Optional.empty();
    private Optional<MountType> previousMountType = Optional.empty();
    @Persisted
    private final Storage<Map<MountEnclosure, List<MountEnclosureInfo>>> mountEnclosures =
            new Storage<>(new EnumMap<>(MountEnclosure.class));
    @Persisted
    private final Config<Integer> secondsToAddIfUnknown = new Config<>(60);
    private MountEnclosure currentMountEnclosure = null;
    private static final int[] mountEnclosureMainSlots = {9, 18, 27, 36, 45};
    private static final int[] mountEnclosureSlots = {
        11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42, 47, 48, 49, 50, 51
    };
    private final int[] mountEnclosureSBSSlots = {16, 17, 25, 26, 34, 35, 43, 44, 52, 53};
    private static final Pattern MOUNT_TIME_PATTERN = Pattern.compile(
            ".*?(?:Feeding|Breeding) in(?: (?<hour>\\d+)h)?(?: (?<minute>\\d+)m)?(?: (?<second>\\d+)s)?");

    public MountModel() {
        super(List.of());
        Handlers.Label.registerParser(new MountEnclosureLabelParser());
        Handlers.ActionBar.registerSegment(new MountEnergySegmentMatcher());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MOUNT_COLORS).handleReader(this::handleMountColors);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.InteractAt event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onUseItemOn(PlayerInteractEvent.RightClickBlock event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onSwing(ArmSwingEvent event) {
        handleMountItemUse();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (summonTick == -1) return;

        int currentTick = McUtils.player().tickCount;
        if (currentTick - summonTick > MOUNT_TIME_TICKS) {
            // Assume failed to mount
            summonTick = -1;
            expectedMountType = null;
        }

        if (dismountTick != -1 && currentTick > dismountTick) {
            dismountTick = -1;
        }
    }

    @SubscribeEvent
    public void onVehicleChange(SetLocalPlayerVehicleEvent event) {
        if (!Models.WorldState.onWorld()) return;

        if (event.getVehicle() == null && currentMountType.isPresent()) {
            previousMountType = currentMountType;
            currentMountType = Optional.empty();
            WynntilsMod.postEvent(new MountEvent.Dismount());
            dismountTick = McUtils.player().tickCount;
        } else if (summonTick != -1) {
            currentMountType = Optional.of(expectedMountType);
            expectedMountType = null;
            WynntilsMod.postEvent(new MountEvent.Mount(currentMountType.get(), true));
        } else if (McUtils.player().tickCount == dismountTick && previousMountType.isPresent()) {
            currentMountType = previousMountType;
            previousMountType = Optional.empty();
            WynntilsMod.postEvent(new MountEvent.Mount(currentMountType.get(), false));
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        summonTick = -1;
        expectedMountType = null;
        currentMountType = Optional.empty();
    }

    @SubscribeEvent
    public void onActionBarRender(ActionBarRenderEvent event) {
        if (!hideMountEnergy) return;

        event.setSegmentEnabled(MountEnergySegment.class, false);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(MountEnergySegment.class, this::updateMountEnergy, this::clearMountEnergy);
    }

    @SubscribeEvent
    public void onMountFeederLabelIdentified(LabelIdentifiedEvent e) {
        if (e.getLabelInfo() instanceof MountEnclosureLabelInfo mountEnclosureLabelInfo)
            currentMountEnclosure = mountEnclosureLabelInfo.getMountEnclosure();
    }

    @SubscribeEvent
    public void onMountFeederSetContent(ContainerSetContentEvent.Post e) {
        if (Models.Container.getCurrentContainer() instanceof MountFeederContainer
                && currentMountEnclosure != null
                && e.getItems().size() >= 54) {
            updateMountEnclosure(e.getItems());
        }
    }

    @SubscribeEvent
    public void onMountFeederSetSlot(ContainerSetSlotEvent.Post e) {
        if (Models.Container.getCurrentContainer() instanceof MountFeederContainer
                && currentMountEnclosure != null
                && e.getSlot() <= 53 && mountEnclosures.get().get(currentMountEnclosure) != null) {
            int index = ArrayUtils.indexOf(mountEnclosureMainSlots, e.getSlot());
            boolean isMainSlot = index != -1;
            boolean isSlotValid = isMainSlot && hasRank(e.getItemStack())
                    || ArrayUtils.contains(mountEnclosureSlots, e.getSlot())
                    || Models.Account.isSilverbullSubscriber()
                    && ArrayUtils.contains(mountEnclosureSBSSlots, e.getSlot());
            if (isSlotValid)
            {
                if (isMainSlot)
                {
                    if (!isEmpty(e.getItemStack()) && index == mountEnclosures.get().get(currentMountEnclosure).size() || isEmpty(e.getItemStack()) && index < mountEnclosures.get().get(currentMountEnclosure).size())
                        updateMountEnclosure();
                }
                else
                {
                    index = (e.getSlot() > 46) ? 4 : (e.getSlot() - 9) / 9;
                    int mountItemsIndex = e.getSlot() - (((index + 1) * 9) + 2);
                    if (index < mountEnclosures.get().get(currentMountEnclosure).size() && (!isEmpty(e.getItemStack()) && mountItemsIndex == mountEnclosures.get().get(currentMountEnclosure).get(index).mountItems().size() || isEmpty(e.getItemStack()) && mountItemsIndex < mountEnclosures.get().get(currentMountEnclosure).get(index).mountItems().size()))
                        updateMountEnclosure();
                }
            }
        }
    }

    public Map<MountEnclosure, List<MountEnclosureInfo>> getMountEnclosures()
    {
        return Collections.unmodifiableMap(mountEnclosures.get());
    }

    public List<MountEnclosureInfo> getMountEnclosure(MountEnclosure mountEnclosure)
    {
        if (!mountEnclosures.get().containsKey(mountEnclosure))
            return List.of();
        return Collections.unmodifiableList(mountEnclosures.get().get(mountEnclosure));
    }

    public void tryRideMount(MountChoice mountChoice) {
        if (!Models.WorldState.onWorld()) return;

        LocalPlayer player = McUtils.player();
        if (player.getVehicle() != null) {
            postMountErrorMessage(RideMountStatus.ALREADY_RIDING);
            return;
        }

        int mountInventorySlot = findMountSlotNum(mountChoice);
        if (mountInventorySlot == -1) {
            postMountErrorMessage(RideMountStatus.NO_MOUNT);
            return;
        }
        if (mountInventorySlot > 8) {
            postMountErrorMessage(RideMountStatus.CONFLICTING_SLOTS);
            return;
        }

        Optional<MountItem> mountItem = getMount(mountChoice);
        if (mountItem.isEmpty()) {
            postMountErrorMessage(RideMountStatus.NO_MOUNT);
            return;
        } else {
            expectedMountType = mountItem.get().getMountType();
        }

        WynntilsMod.postEvent(new MountEvent.Summon(expectedMountType));
        McUtils.sendPacket(new ServerboundSetCarriedItemPacket(mountInventorySlot));
        Managers.TickScheduler.scheduleNextTick(() -> {
            MouseUtils.sendRightClickInput();
            McUtils.sendPacket(new ServerboundSetCarriedItemPacket(McUtils.inventory().selected));
            summonTick = McUtils.player().tickCount;
        });
    }

    public Optional<MountItem> getMount(MountChoice mountChoice) {
        int mountSlot = findMountSlotNum(mountChoice);
        if (mountSlot == -1) return Optional.empty();

        return Models.Item.asWynnItem(McUtils.inventory().getItem(mountSlot), MountItem.class);
    }

    public int findMountSlotNum(MountChoice mountChoice) {
        Inventory inventory = McUtils.inventory();
        for (int slotNum = 0; slotNum < Inventory.INVENTORY_SIZE; slotNum++) {
            ItemStack itemStack = inventory.getItem(slotNum);
            Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);

            if (mountItemOpt.isPresent()) {
                if (mountChoice == MountChoice.FIRST
                        || mountChoice.getMountType() == mountItemOpt.get().getMountType()) {
                    return slotNum;
                }
            }
        }

        return -1;
    }

    public MountColorInfo getMountColor(int id) {
        return mountColors.values().stream()
                .filter(mountColorInfo -> mountColorInfo.id() == id)
                .findFirst()
                .orElse(MountColorInfo.UNKNOWN);
    }

    public MountColorInfo getMountColor(String displayName) {
        MountColorInfo mountColorInfo = mountColors.get(displayName);

        if (mountColorInfo == null) {
            WynntilsMod.warn("Unknown mount color info: " + displayName);
            return MountColorInfo.UNKNOWN;
        }

        return mountColorInfo;
    }

    public boolean isValidColor(MountColorInfo mountColorInfo, MountType mountType, ColorType colorType) {
        boolean validColor = false;
        for (MountColorType mountColorType : mountColorInfo.mounts()) {
            if (mountColorType.mount() == mountType && mountColorType.type() == colorType) {
                validColor = true;
                break;
            }
        }

        return validColor;
    }

    public Optional<MountType> getCurrentMountType() {
        return currentMountType;
    }

    public void setHideMountEnergy(boolean hide) {
        hideMountEnergy = hide;
    }

    public Optional<CappedValue> getCurrentMountEnergy() {
        if (currentMountEnergy == CappedValue.EMPTY) return Optional.empty();
        return Optional.of(currentMountEnergy);
    }

    private void handleMountItemUse() {
        if (!Models.WorldState.onWorld()) return;

        ItemStack itemStack = McUtils.inventory().getSelectedItem();
        Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(itemStack, MountItem.class);
        if (mountItemOpt.isEmpty()) return;
        if (!mountItemOpt.get().isSummonItem()) return;

        expectedMountType = mountItemOpt.get().getMountType();

        summonTick = McUtils.player().tickCount;
        WynntilsMod.postEvent(new MountEvent.Summon(expectedMountType));
    }

    private void handleMountColors(Reader reader) {
        Type type = new TypeToken<List<MountColorInfo>>() {}.getType();
        List<MountColorInfo> mountColorsList = Managers.Json.GSON.fromJson(reader, type);

        mountColors = mountColorsList.stream()
                .collect(Collectors.toMap(MountColorInfo::displayName, mountColorInfo -> mountColorInfo));
    }

    private void updateMountEnergy(MountEnergySegment segment) {
        currentMountEnergy = segment.getCappedEnergy();
    }

    private void clearMountEnergy() {
        currentMountEnergy = CappedValue.EMPTY;
    }

    private void postMountErrorMessage(RideMountStatus status) {
        Managers.Notification.queueMessage(
                Component.translatable(status.getTranslationKey()).withStyle(ChatFormatting.DARK_RED));
    }

    private void updateMountEnclosure()
    {
        updateMountEnclosure(McUtils.containerMenu().getItems());
    }

    private void updateMountEnclosure(List<ItemStack> mountEnclosureItems)
    {
        List<MountEnclosureInfo> mountEnclosureInfos = new ArrayList<>(5);
        for (int index = 0; index < mountEnclosureMainSlots.length; index++)
        {
            int slot = mountEnclosureMainSlots[index];
            ItemStack mountItemItemStack = mountEnclosureItems.get(slot);
            if (isEmpty(mountItemItemStack) || index > 1 && !hasRank(mountItemItemStack))
                break;
            Matcher mountTimeMatcher = LoreUtils.getLore(mountItemItemStack).stream().map(StyledText::getStringWithoutFormatting).map(MOUNT_TIME_PATTERN::matcher).filter(Matcher::matches).findFirst().orElse(null);
            // Wait for the mount time to be updated after adding/removing the first/last mount food
            if (mountTimeMatcher == null && !isEmpty(mountEnclosureItems.get(slot + 2)) || mountTimeMatcher != null && isEmpty(mountEnclosureItems.get(slot + 2)))
                return;
            // Even tho isEmpty should cover this, just in case check if the optinal is empty
            Optional<MountItem> mountItemOpt = Models.Item.asWynnItem(mountItemItemStack, MountItem.class);
            if (mountItemOpt.isEmpty())
                break;
            Mount mount = new Mount(mountItemOpt.get().getName(), mountItemOpt.get().getMountInfo().potential(), mountItemOpt.get().getMountInfo().primaryColorInfo().displayName(), mountItemOpt.get().getMountInfo().secondaryColorInfo().displayName(), mountItemOpt.get().getMountInfo().currentEnergy(), mountItemOpt.get().getMountInfo().stats(), mountItemOpt.get().getMountInfo().maxStats(), mountItemOpt.get().getMountType());
            List<Object> mountItems = new ArrayList<>(7);
            Time time = Time.NONE;
            if (mountTimeMatcher != null)
            {
                for (int i = slot + 2; i <= (Models.Account.isSilverbullSubscriber() ? slot + 8 : slot + 6); i++)
                {
                    ItemStack mountItemsItemStack = mountEnclosureItems.get(i);
                    if (isEmpty(mountItemsItemStack))
                        break;
                    Optional<MaterialItem> materialItemOpt = Models.Item.asWynnItem(mountItemsItemStack, MaterialItem.class);
                    if (materialItemOpt.isPresent())
                        MountFood.fromMaterialItem(materialItemOpt.get()).ifPresent(mountItems::add);
                    else
                        Models.Item.asWynnItem(mountItemsItemStack, MountItem.class).ifPresent(mountItem -> mountItems.add(new Mount(mountItem.getName(), mountItem.getMountInfo().potential(), mountItem.getMountInfo().primaryColorInfo().displayName(), mountItem.getMountInfo().secondaryColorInfo().displayName(), mountItem.getMountInfo().currentEnergy(), mountItem.getMountInfo().stats(), mountItem.getMountInfo().maxStats(), mountItem.getMountType())));
                }
                // Calculate the timestamp taking into account the limit changes caused by mount foods.
                // It's basically impossible to calculate the stat and color changes caused by breeding so a warning will be shown
                long timestamp = 0;
                Mount mount1 = new Mount(
                        mount.name(),
                        mount.potential(),
                        mount.primaryColor(),
                        mount.secondaryColor(),
                        mount.currentEnergy(),
                        mount.stats(),
                        mount.maxStats(),
                        mount.mountType());
                for (int i = 1; i < mountItems.size(); i++) {
                    Object mountItemsItem = mountItems.get(i);
                    boolean instanceofMount = mountItemsItem instanceof Mount;
                    int averageLimit = 20;
                    if (!instanceofMount)
                        averageLimit = (int) Math.ceil(mount1.stats().values().stream()
                                .mapToInt(CappedValue::max)
                                .average()
                                .orElse(0.0));
                    timestamp += switch (averageLimit) {
                        case 10 -> Duration.ofMinutes(1).toMillis();
                        case 12 -> Duration.ofMinutes(5).toMillis();
                        case 13 -> Duration.ofMinutes(15).toMillis();
                        case 14 -> Duration.ofMinutes(30).toMillis();
                        case 15 -> Duration.ofHours(1).toMillis();
                        case 16 -> Duration.ofHours(2).toMillis();
                        case 17 -> Duration.ofHours(3).toMillis();
                        case 18 -> Duration.ofHours(4).toMillis();
                        case 19 -> Duration.ofHours(5).toMillis();
                        default ->
                        {
                            if (averageLimit >= 20)
                                yield Duration.ofHours(6).toMillis();
                            else
                                yield 0;
                        }
                    };
                    if (!instanceofMount) {
                        MountFood mountFood = (MountFood) mountItemsItem;
                        for (Map.Entry<MountStat, CappedValue> statsEntry :
                                mount1.stats().entrySet()) {
                            if (statsEntry.getValue().max() == mount1.maxStats().get(statsEntry.getKey())) continue;
                            int increaseAmount = 0;
                            if (statsEntry.getKey() == mountFood.getFirstMountStat().key())
                                increaseAmount = statsEntry.getValue().max()
                                        + mountFood.getFirstMountStat().value();
                            else if (statsEntry.getKey() == mountFood.getSecondMountStat().key())
                                increaseAmount = statsEntry.getValue().max()
                                        + mountFood.getSecondMountStat().value();
                            else if (mountFood.getThirdMountStat() != null
                                    && statsEntry.getKey() == mountFood.getThirdMountStat().key())
                                increaseAmount = statsEntry.getValue().max()
                                        + mountFood.getThirdMountStat().value();
                            if (increaseAmount == 0) continue;
                            if (increaseAmount > mount1.maxStats().get(statsEntry.getKey()))
                                increaseAmount = mount1.maxStats().get(statsEntry.getKey());
                            statsEntry.setValue(new CappedValue(statsEntry.getValue().current(), increaseAmount));
                        }
                    }
                }
                String hour = mountTimeMatcher.group("hour");
                String minute = mountTimeMatcher.group("minute");
                String second = mountTimeMatcher.group("second");
                int seconds = secondsToAddIfUnknown.get() * 1000;
                if (seconds < 0)
                    seconds = 0;
                timestamp += Duration.ofHours(hour == null ? 0 : Long.parseLong(hour))
                        .plusMinutes(minute == null ? 0 : Long.parseLong(minute))
                        .plusSeconds(second == null ? seconds : Long.parseLong(second))
                        .toMillis();
                if (timestamp != 0)
                    time = Time.of(timestamp + System.currentTimeMillis());
            }
            mountEnclosureInfos.add(new MountEnclosureInfo(mount, mountItems, time));
        }
        if (mountEnclosures.get().get(currentMountEnclosure) == null || !mountEnclosures.get().get(currentMountEnclosure).equals(mountEnclosureInfos))
        {
            mountEnclosures.get().put(currentMountEnclosure, mountEnclosureInfos);
            mountEnclosures.touched();
        }
    }

    private boolean isEmpty(ItemStack mountEnclosureItemStack) {
        StyledText styledText = StyledText.fromComponent(mountEnclosureItemStack.getHoverName());
        return styledText.equalsString("Drag a mount into this slot to", StyleType.NONE)
                || styledText.equalsString("\uDB3F\uDFFF", StyleType.NONE)
                || mountEnclosureItemStack.isEmpty();
    }

    //TODO: replace with a non-API method of getting the player's rank when it is made
    private boolean hasRank(ItemStack mountItemItemStack) {
        return !StyledText.fromComponent(mountItemItemStack.getHoverName())
                .endsWith(" Rank or higher is", StyleType.NONE);
    }

    private enum RideMountStatus {
        NO_MOUNT("model.wynntils.mount.noMount"),
        ALREADY_RIDING("model.wynntils.mount.alreadyRiding"),
        CONFLICTING_SLOTS("model.wynntils.mount.conflictingSlots");

        private final String translationKey;

        RideMountStatus(String tcString) {
            this.translationKey = tcString;
        }

        private String getTranslationKey() {
            return this.translationKey;
        }
    }
}