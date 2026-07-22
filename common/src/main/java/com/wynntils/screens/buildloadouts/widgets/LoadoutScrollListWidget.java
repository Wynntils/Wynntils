package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.StringUtils;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LoadoutScrollListWidget extends ScrollListWidget {
    private static final int MAX_WIDGETS_PER_PAGE = 7;
    private static final int WIDTH = 133 - 10;
    private static final int HEIGHT = 251 - 5;
    private static final int WIDGET_HEIGHT = 32;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;

    public LoadoutScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, WIDTH, HEIGHT, WIDGET_HEIGHT, WIDGET_HEIGHT_PADDING, WIDGET_HEIGHT_EDGE_PADDING, MAX_WIDGETS_PER_PAGE);
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

        Map<String, Loadout> savedLoadouts = new TreeMap<>(Services.loadout.getLoadouts());

        ClassType currentClass = Models.Character.getClassType();

        List<String> filteredSorted = savedLoadouts.keySet().stream()
                .filter(this::searchMatches)
                .sorted(Comparator
                        .comparingInt((String name) -> getSortRank(savedLoadouts.get(name), currentClass))
                        .thenComparing(Comparator.naturalOrder()))
                .toList();

        for (String name : filteredSorted) {
            Loadout loadout = savedLoadouts.get(name);

            if (parent.getCurrentCategory() != loadout.getMenuCategory()) continue;

            parent.loadoutWidgets.add(new LoadoutWidget(
                    StyledText.fromString(loadout.name()),
                    this.x + 5,
                    this.y + 5 + parent.loadoutWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                    133 - 25 - 4,
                    WIDGET_HEIGHT,
                    loadout,
                    parent));
        }
    }

    /**
     * Lower rank sorts first:
     * 0 - current class, favourited
     * 1 - current class, not favourited
     * 2 - other class, favourited
     * 3 - other class, not favourited
     * 4 - no class (e.g. skill point loadouts), favourited
     * 5 - no class, not favourited
     */
    private int getSortRank(Loadout loadout, ClassType currentClass) {
        boolean favourited = loadout.favourited();

        if (!loadout.hasClassType()) {
            return favourited ? 4 : 5;
        }

        boolean isCurrentClass = loadout.getClassType() == currentClass;

        if (isCurrentClass) {
            return favourited ? 0 : 1;
        } else {
            return favourited ? 2 : 3;
        }
    }

    private boolean searchMatches(String name) {
        return StringUtils.partialMatch(name, parent.searchWidget.getTextBoxInput());
    }
}
