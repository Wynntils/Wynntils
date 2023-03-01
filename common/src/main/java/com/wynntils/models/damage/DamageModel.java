/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.damage;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.models.damage.type.DamageDealtEvent;
import com.wynntils.models.damage.type.FocusedDamageEvent;
import com.wynntils.models.stats.type.DamageType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DamageModel extends Model {
    // https://regexr.com/7968a
    private static final Pattern DAMAGE_LABEL_PATTERN = Pattern.compile(
            "^(?:§4-(\\d+) ❤ )?(?:§e-(\\d+) ✦ )?(?:§2-(\\d+) ✤ )?(?:§b-(\\d+) ❉ )?(?:§f-(\\d+) ❋ )?(?:§c-(\\d+) ✹ )?$");

    // https://regexr.com/7965g
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile("^§[ac](.*)§r - §c(\\d+)§4❤(?:§r - §7(.*)§7)?$");

    private static final List<DamageType> LABEL_ELEMENT_ORDER = List.of(
            DamageType.NEUTRAL,
            DamageType.THUNDER,
            DamageType.EARTH,
            DamageType.WATER,
            DamageType.AIR,
            DamageType.FIRE);

    private final DamageBar damageBar = new DamageBar();

    private String focusedMobName;
    private String focusedMobElementals;
    private int focusedMobHealth;
    private long lastDamageDealtTimestamp;

    public DamageModel() {
        super(List.of());
        Handlers.BossBar.registerBar(damageBar);
    }

    public long getLastDamageDealtTimestamp() {
        return lastDamageDealtTimestamp;
    }

    @SubscribeEvent
    public void onLabelChange(EntityLabelChangedEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;

        Matcher matcher = DAMAGE_LABEL_PATTERN.matcher(event.getName());
        if (!matcher.matches()) return;

        Map<DamageType, Integer> damages = new HashMap<>();
        for (int i = 0; i < LABEL_ELEMENT_ORDER.size(); i++) {
            String damageStr = matcher.group(i + 1);
            if (damageStr == null) continue;

            int damage = Integer.parseInt(damageStr);
            damages.put(LABEL_ELEMENT_ORDER.get(i), damage);
        }

        WynntilsMod.postEvent(new DamageDealtEvent(damages));
        lastDamageDealtTimestamp = System.currentTimeMillis();
    }

    public final class DamageBar extends TrackedBar {
        public DamageBar() {
            super(DAMAGE_BAR_PATTERN);
        }

        @Override
        public void onUpdateName(Matcher match) {
            String mobName = match.group(1);
            int health = Integer.parseInt(match.group(2));
            String mobElementals = match.group(3);
            if (mobElementals == null) {
                mobElementals = "";
            }

            if (mobName.equals(focusedMobName) && mobElementals.equals(focusedMobElementals)) {
                int oldHealth = focusedMobHealth;
                focusedMobHealth = health;

                WynntilsMod.postEvent(new FocusedDamageEvent.MobDamaged(
                        focusedMobName, focusedMobElementals, focusedMobHealth, oldHealth));
            } else {
                focusedMobName = mobName;
                focusedMobElementals = mobElementals;
                focusedMobHealth = health;

                WynntilsMod.postEvent(
                        new FocusedDamageEvent.MobFocused(focusedMobName, focusedMobElementals, focusedMobHealth));
            }
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }

        @Override
        public void onUpdateProgress(float progress) {
            lastDamageDealtTimestamp = System.currentTimeMillis();
        }
    }
}
