/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.bossbar;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.models.combat.type.MobElementals;
import com.wynntils.models.elements.type.Element;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DamageBar extends TrackedBar {
    // Test in DamageBar_DAMAGE_BAR_PATTERN
    private static final Pattern DAMAGE_BAR_PATTERN = Pattern.compile(
            "^\\s*§(?:#)?[0-9a-f]{1,8}(?:§l)?(.*?)§r - §c(\\d+(?:\\.\\d+)?[km]?)§4❤(?:§r - ((?:§.|\\S)*?Weak))?(?:.*?((?:§.|\\S)*?Dam))?(?:.*?((?:§.|\\S)*?Def))?\\s*$");

    private static final Pattern ELEMENT_PATTERN = Pattern.compile("§[0-9a-f]("
            + Arrays.stream(Element.values())
                    .map(e -> Pattern.quote(e.getSymbol()))
                    .collect(Collectors.joining("|"))
            + ")");

    public DamageBar() {
        super(DAMAGE_BAR_PATTERN);
    }

    @Override
    public void onUpdateName(Matcher match) {
        String mobName = match.group(1);
        long health = StringUtils.parseSuffixedInteger(match.group(2));
        List<Element> weaknesses = parseElements(match.group(3));
        List<Element> damages = parseElements(match.group(4));
        List<Element> defenses = parseElements(match.group(5));
        MobElementals mobElementals = new MobElementals(weaknesses, damages, defenses);

        Models.Combat.checkFocusedMobValidity();
        if (mobName.equals(Models.Combat.getFocusedMobName())
                && mobElementals.equals(Models.Combat.getFocusedMobElementals())) {
            Models.Combat.updateFocusedMobHealth(health);
        } else {
            Models.Combat.updateFocusedMob(mobName, mobElementals, health);
        }
        Models.Combat.revalidateFocusedMob();
        Models.Combat.setLastDamageDealtTimestamp(System.currentTimeMillis());
    }

    @Override
    public void onUpdateProgress(float progress) {
        Models.Combat.updateFocusedMobHealthPercent(new CappedValue(Math.round(progress * 100), 100));
        Models.Combat.revalidateFocusedMob();
    }

    @Override
    protected void reset() {
        Models.Combat.invalidateFocusedMob();
    }

    private List<Element> parseElements(String elementPart) {
        List<Element> elements = new ArrayList<>();
        if (elementPart == null || elementPart.isEmpty()) return elements;

        Matcher matcher = ELEMENT_PATTERN.matcher(elementPart);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            Element element = Element.fromSymbol(symbol);

            if (element != null && !elements.contains(element)) {
                elements.add(element);
            }
        }

        return elements;
    }
}
