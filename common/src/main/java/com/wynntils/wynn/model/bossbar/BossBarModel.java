/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.CustomBarAddEvent;
import com.wynntils.mc.utils.ComponentUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BossBarModel extends Model {

    public static final TrackedBar manaBankBar = new TrackedBar(Pattern.compile("§bMana Bank §3\\[(\\d+)/(\\d+)§3\\]"), TrackedBar.BarType.MANABANK) {
        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format("Failed to parse current and max for mana bank bar %s (%s out of %s)", type, match.group(1), match.group(2)));
            }
        }
    };

    public static final TrackedBar bloodPoolBar = new TrackedBar(Pattern.compile("§cBlood Pool §4\\[§c(\\d+)%§4\\]"), TrackedBar.BarType.AWAKENED) {
        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format("Failed to parse current and max for blood pool bar (%s out of %s)", match.group(1), match.group(2)));
            }
        }

        @Override
        public void onAdd() {
            if (progress == 0f) {
                WynntilsMod.error("Progress of zero for blood pool bar, can not find max");
            } else {
                // Round to nearest 30
                max = Math.round(current / (progress * 30f)) * 30;
            }
        }
    };

    public static final TrackedBar awakenedBar = new TrackedBar(Pattern.compile("§fAwakening §7\\[§f(\\d+)/(\\d+)§7]"), TrackedBar.BarType.AWAKENED) {
        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format("Failed to parse current and max for awakened bar %s (%s out of %s)", type, match.group(1), match.group(2)));
            }
        }
    };

    public static final TrackedBar focusBar = new TrackedBar(Pattern.compile("§eFocus §6\\[§e(\\d+)/(\\d+)§6]"), TrackedBar.BarType.FOCUS) {
        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
                max = Integer.parseInt(match.group(2));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format("Failed to parse current and max for focus bar %s (%s out of %s)", type, match.group(1), match.group(2)));
            }
        }
    };

    public static final TrackedBar corruptedBar = new TrackedBar(Pattern.compile("§cCorrupted §4\\[§c(\\d+)%§4]"), TrackedBar.BarType.CORRUPTED) {
        @Override
        public void onUpdateName(Matcher match) {
            try {
                current = Integer.parseInt(match.group(1));
            } catch (NumberFormatException e) {
                WynntilsMod.error(String.format("Failed to parse current and max for corrupted bar %s (%s out of %s)", type, match.group(1), match.group(2)));
            }
        }

        @Override
        public void onAdd() {
            max = 100;
        }
    };

    private static final List<TrackedBar> trackedBars = Arrays.asList(manaBankBar, bloodPoolBar, awakenedBar, focusBar, corruptedBar);

    @SubscribeEvent
    private static void onHealthBarEvent(BossHealthUpdateEvent event) {
        ClientboundBossEventPacket packet = event.getPacket();

        packet.dispatch(new TrackedBarHandler(event));

    }

    private static TrackedBar matchUUID(UUID id) {

        for (TrackedBar trackedBar : trackedBars) {
            if (trackedBar.getUuid().equals(id)) {
                return trackedBar;
            }
        }

        return null;

    }

    public record BarProgress(int current, int max, float progress) {}
    
    private record TrackedBarHandler(BossHealthUpdateEvent event) implements ClientboundBossEventPacket.Handler {

        public void add(UUID id, Component name, float progress, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
            TrackedBar bar = null;
            Matcher matcher = null;

            for (TrackedBar trackedBar : trackedBars) {
                matcher = trackedBar.pattern.matcher(ComponentUtils.getCoded(name));
                if (matcher.matches()) {
                    bar = trackedBar;
                    break;
                }
            }

            if (bar == null) return;

            CustomBarAddEvent barAddEvent = new CustomBarAddEvent(bar.type);
            WynntilsMod.postEvent(barAddEvent);

            bar.setProgress(progress);
            bar.setUuid(id);

            if (barAddEvent.isCanceled()) {
                bar.setRendered(false);
                event.setCanceled(true);
            } else {
                bar.setRendered(true);
            }

            // Order matters
            bar.onUpdateName(matcher);
            bar.onAdd();
        }

        public void remove(UUID id) {
            TrackedBar bar = matchUUID(id);

            if (bar != null) {
                if (!bar.isRendered()) {
                    event.setCanceled(true);
                }

                bar.reset();
            }
        }

        public void updateProgress(UUID id, float progress) {
            TrackedBar bar = matchUUID(id);

            if (bar != null) {
                if (!bar.isRendered()) {
                    event.setCanceled(true);
                }

                bar.setProgress(progress);
            }
        }

        public void updateName(UUID id, Component name) {
            TrackedBar bar = matchUUID(id);

            if (bar != null) {
                if (!bar.isRendered()) {
                    event.setCanceled(true);

                    Matcher matcher = bar.pattern.matcher(ComponentUtils.getCoded(name));
                    if (!matcher.matches()) {
                        WynntilsMod.error("Failed to match already matched boss bar");
                    }

                    bar.onUpdateName(matcher);
                }
            }
        }

        public void updateStyle(UUID id, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
            TrackedBar bar = matchUUID(id);

            if (bar != null && !bar.isRendered()) {
                event.setCanceled(true);
            }

        }

        public void updateProperties(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
            TrackedBar bar = matchUUID(id);

            if (bar != null && !bar.isRendered()) {
                event.setCanceled(true);
            }
        }
    }
}
