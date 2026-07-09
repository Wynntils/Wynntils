package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.screens.buildloadouts.type.Loadout;
import com.wynntils.screens.buildloadouts.type.LoadoutType;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class LoadoutScrollListWidget extends ScrollListWidget {
    private static final int WIDTH = 133 - 10;
    private static final int HEIGHT = 251 - 5;
    private static final int WIDGET_HEIGHT = 32;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, WIDTH, HEIGHT, WIDGET_HEIGHT, WIDGET_HEIGHT_PADDING, WIDGET_HEIGHT_EDGE_PADDING);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        return Collections.unmodifiableList(parent.loadoutWidgets);
    }

    public void populateLoadouts() {
        parent.loadoutWidgets = new ArrayList<>();

        Map<String, SavableSkillPointSet> spLoadouts = new TreeMap<>(Models.SkillPoint.getLoadouts());
        Map<String, SavableAbilityTree> atLoadouts = Models.AbilityTree.getAbilityTreeLoadouts();
        Map<String, SavableAspectSet> aspectLoadouts = Models.Aspect.getAspectLoadouts();

        Set<String> allNames = new HashSet<>();
        allNames.addAll(spLoadouts.keySet());
        allNames.addAll(atLoadouts.keySet());
        allNames.addAll(aspectLoadouts.keySet());

        for (String name : new TreeSet<>(allNames)) {
            SavableSkillPointSet sp = spLoadouts.get(name);
            SavableAbilityTree at = atLoadouts.get(name);
            SavableAspectSet aspect = aspectLoadouts.get(name);
            Loadout loadout = new Loadout(name, sp, at, aspect, determineLoadoutType(sp, at, aspect));

            parent.loadoutWidgets.add(new LoadoutWidget(
                    StyledText.fromString(loadout.name()),
                    this.x + 5,
                    this.y + 5 + parent.loadoutWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                    133 - 25,
                    WIDGET_HEIGHT,
                    loadout,
                    parent));
        }
    }

    private LoadoutType determineLoadoutType(SavableSkillPointSet sp, SavableAbilityTree at, SavableAspectSet aspect) {
        boolean hasSp = sp != null;
        boolean hasAt = at != null;
        boolean hasAspect = aspect != null;
        if (hasSp && sp.isBuild()) return LoadoutType.BUILD;
        if (hasAt && !hasSp) return LoadoutType.ABILITY_TREE;
        if (hasAspect && !hasSp && !hasAt) return LoadoutType.ASPECT;
        return LoadoutType.SKILL_POINT;
    }
}
