/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.CustomBarAddEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.objects.ClassType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossBarModel extends Model {
    public static final TrackedBar manaBankBar = new ManaBankBar();

    public static final TrackedBar bloodPoolBar = new BloodPoolBar();

    public static final TrackedBar awakenedBar = new AwakenedBar();

    public static final TrackedBar focusBar = new FocusBar();

    public static final TrackedBar corruptedBar = new CorruptedBar();

    private static final HashMap<UUID, TrackedBar> trackedBarsMap = new HashMap<>();

    // FixPacketBugsFeature gets in the way if receiveCanceled is not set
    @SubscribeEvent(receiveCanceled = true)
    public void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();

        packet.dispatch(new TrackedBarHandler(event));
    }

    public record BarProgress(int current, int max, float progress) {}

    private record TrackedBarHandler(BossHealthUpdateEvent event) implements ClientboundBossEventPacket.Handler {
        @Override
        public void add(
                UUID id,
                Component name,
                float progress,
                BossEvent.BossBarColor color,
                BossEvent.BossBarOverlay overlay,
                boolean darkenScreen,
                boolean playMusic,
                boolean createWorldFog) {
            TrackedBar trackedBar = null;
            Matcher matcher = null;

            ClassType userClass = Managers.Character.getCharacterInfo().getClassType();

            for (TrackedBar potentialTrackedBar :
                    Arrays.asList(manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar)) {
                if (potentialTrackedBar.classType != userClass) continue;

                matcher = potentialTrackedBar.pattern.matcher(ComponentUtils.getCoded(name));
                if (matcher.matches()) {
                    trackedBar = potentialTrackedBar;
                    break;
                }
            }

            if (trackedBar == null) return;

            event.setCanceled(true);

            LerpingBossEvent bossEvent =
                    new LerpingBossEvent(id, name, progress, color, overlay, darkenScreen, playMusic, createWorldFog);
            trackedBar.setEvent(bossEvent);

            // Allow for others to try and cancel event
            CustomBarAddEvent barAddEvent = new CustomBarAddEvent(trackedBar.type);
            WynntilsMod.postEvent(barAddEvent);

            if (barAddEvent.isCanceled()) {
                trackedBar.setRendered(false);
            } else {
                trackedBar.setRendered(true);
                McUtils.mc().gui.getBossOverlay().events.put(id, bossEvent);
            }

            trackedBar.onUpdateName(matcher);

            trackedBarsMap.put(id, trackedBar);
        }

        private void handleBarUpdate(UUID id, Consumer<TrackedBar> consumer) {
            TrackedBar trackedBar = trackedBarsMap.get(id);

            if (trackedBar != null) {
                if (!trackedBar.isRendered()) {
                    event.setCanceled(true);
                }

                consumer.accept(trackedBar);
            }
        }

        @Override
        public void remove(UUID id) {
            handleBarUpdate(id, trackedBar -> {
                trackedBar.reset();
                trackedBarsMap.remove(id);
            });
        }

        @Override
        public void updateProgress(UUID id, float progress) {
            handleBarUpdate(id, trackedBar -> {
                event.setCanceled(true);
                trackedBar.getEvent().setProgress(progress);
                trackedBar.onUpdateProgress(progress);
            });
        }

        @Override
        public void updateName(UUID id, Component name) {
            handleBarUpdate(id, trackedBar -> {
                Matcher matcher = trackedBar.pattern.matcher(ComponentUtils.getCoded(name));
                if (!matcher.matches()) {
                    WynntilsMod.error("Failed to match already matched boss bar");
                    return;
                }

                trackedBar.onUpdateName(matcher);
            });
        }

        // We need to cancel the event even though we don't process it here
        @Override
        public void updateStyle(UUID id, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
            handleBarUpdate(id, trackedBar -> {});
        }

        @Override
        public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
            handleBarUpdate(id, trackedBar -> {});
        }
    }

    private static class ManaBankBar extends TrackedBar {
        public ManaBankBar() {
            super(Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)§3\\]"), BarType.MANABANK, ClassType.Mage);
        }

        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format(
                        "Failed to parse current and max for mana bank bar %s (%s out of %s)",
                        type, match.group(1), match.group(2)));
            }
        }
    }

    private static class BloodPoolBar extends TrackedBar {
        public BloodPoolBar() {
            super(Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]"), BarType.BLOODPOOL, ClassType.Shaman);
        }

        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format(
                        "Failed to parse current for blood pool bar (%s out of %s)", match.group(1), match.group(2)));
            }
        }

        // Wynncraft sends the name packet before the progress packet
        @Override
        public void onUpdateProgress(float progress) {
            if (progress != 0f) {
                // Round to nearest 30
                int unroundedMax = (int) (current / progress);
                int remainder = unroundedMax % 30;

                max = unroundedMax - remainder;
                if (remainder > 15) {
                    max += 30;
                }
            }
        }
    }

    private static class AwakenedBar extends TrackedBar {
        public AwakenedBar() {
            super(Pattern.compile("§fAwakening §7\\[§f(\\d+)/(\\d+)§7]"), BarType.AWAKENED, ClassType.Shaman);
        }

        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format(
                        "Failed to parse current and max for awakened bar %s (%s out of %s)",
                        type, match.group(1), match.group(2)));
            }
        }
    }

    private static class FocusBar extends TrackedBar {
        public FocusBar() {
            super(Pattern.compile("§eFocus §6\\[§e(\\d+)/(\\d+)§6]"), BarType.FOCUS, ClassType.Archer);
        }

        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format(
                        "Failed to parse current and max for focus bar %s (%s out of %s)",
                        type, match.group(1), match.group(2)));
            }
        }
    }

    private static class CorruptedBar extends TrackedBar {
        public CorruptedBar() {
            super(Pattern.compile("§cCorrupted §4\\[§c(\\d+)%§4]"), BarType.CORRUPTED, ClassType.Warrior);
        }

        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = 100;
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format(
                        "Failed to parse current and max for corrupted bar %s (%s out of %s)",
                        type, match.group(1), match.group(2)));
            }
        }
    }
}
