package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.wynn.event.SpellCastedEvent;
import com.wynntils.wynn.model.actionbar.SpellSegment;
import com.wynntils.wynn.objects.SpellType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellModel extends Model {
    private static final Pattern LEVEL_1_SPELL_PATTERN = Pattern.compile("§a(Left|Right|\\?)§7-§a(Left|Right|\\?)§7-§r§a(Left|Right|\\?)§r");
    private static final Pattern LOW_LEVEL_SPELL_PATTERN = Pattern.compile("§a([LR?])§7-§a([LR?])§7-§r§a([LR?])§r");

    @SubscribeEvent
    public void onSpellSegmentUpdate(SpellSegment.SpellSegmentUpdateEvent e) {
        Matcher matcher = e.getMatcher();
        if (!matcher.matches()) return;

        if (matcher.group(3) != null && !matcher.group(3).equals("?")) {
            boolean[] lastSpell = new boolean[3];
            lastSpell[0] = matcher.group(1).charAt(0) == 'R' ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
            lastSpell[1] = matcher.group(2).charAt(0) == 'R' ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
            lastSpell[2] = matcher.group(3).charAt(0) == 'R' ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
            SpellCastedEvent spellCasted = new SpellCastedEvent(SpellType.fromBooleanArray(lastSpell));
            WynntilsMod.postEvent(spellCasted);
        }
    }

    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent e) {
        int level = Managers.Character.getXpLevel();
        String right = (level == 1) ? "Right" : "R";
        Matcher m = (level == 1 ? LEVEL_1_SPELL_PATTERN : LOW_LEVEL_SPELL_PATTERN).matcher(e.getComponent().getString());
        if (!m.matches() || m.group(3).equals("?")) return;

        boolean[] spell = new boolean[3];
        for (int i = 0; i < 3; i++) {
            spell[i] = m.group(i+1).equals(right) ? SpellType.SPELL_RIGHT : SpellType.SPELL_LEFT;
        }
        SpellCastedEvent spellCasted = new SpellCastedEvent(SpellType.fromBooleanArray(spell));
        WynntilsMod.postEvent(spellCasted);
    }

}
