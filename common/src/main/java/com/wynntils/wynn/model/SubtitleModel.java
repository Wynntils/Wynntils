/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.wynn.event.SpellCastedEvent;
import com.wynntils.wynn.objects.SpellType;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SubtitleModel extends Model {
    private static final Pattern LEVEL_1_SPELL_PATTERN =
            Pattern.compile("§a(Left|Right|\\?)§7-§a(Left|Right|\\?)§7-§r§a(Left|Right|\\?)§r");
    private static final Pattern LOW_LEVEL_SPELL_PATTERN = Pattern.compile("§a([LR?])§7-§a([LR?])§7-§r§a([LR?])§r");

    @SubscribeEvent
    public void onSubtitleUpdate(SubtitleSetTextEvent e) {
        int level = WynnUtils.getCharacterInfo().getLevel();
        String right = level == 1 ? "Right" : "R";
        Matcher m = (level == 1 ? LEVEL_1_SPELL_PATTERN : LOW_LEVEL_SPELL_PATTERN)
                .matcher(e.getComponent().getString());
        if (!m.matches() || m.group(1).equals("?") || m.group(3).equals("?")) { // Return if we didn't get a full spell
            return;
        }

        boolean[] spell = new boolean[3];
        spell[0] = m.group(1).equals(right) ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
        spell[1] = m.group(2).equals(right) ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
        spell[2] = m.group(3).equals(right) ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
        SpellCastedEvent spellCasted = new SpellCastedEvent(SpellType.fromBooleanArray(spell));
        WynntilsMod.postEvent(spellCasted);
    }
}
