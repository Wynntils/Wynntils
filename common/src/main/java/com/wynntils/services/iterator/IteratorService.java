/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.iterator;

import com.wynntils.core.components.Service;
import com.wynntils.mc.event.TickEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IteratorService extends Service {
    private final Map<Integer, Iterator> iteratorMap = new HashMap<>();

    public IteratorService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        List<Integer> toRemove = new ArrayList<>();
        iteratorMap.forEach((id, iterator) -> {
            if (iterator.getCurrentTicks() >= iterator.getTicksPerIncrement()) {
                if (iterator.getCurrentIndex() >= iterator.getStringGroups().size() - 1) {
                    toRemove.add(id); // This ensures stale iterators are removed and do not cause memory leaks
                } else {
                    iterator.setCurrentIndex(iterator.getCurrentIndex() + 1);
                }
                iterator.setCurrentTicks(0);
            } else {
                iterator.setCurrentTicks(iterator.getCurrentTicks() + 1);
            }
        });
        toRemove.forEach(iteratorMap::remove);
        toRemove.clear();
    }

    private int getHashCode(String characters, Integer ticksPerIncrement) {
        return (characters + ticksPerIncrement).hashCode();
    }

    public String getIteratorValue(String characters, Integer ticksPerIncrement) {
        int hashCode = getHashCode(characters, ticksPerIncrement);
        if (!iteratorMap.containsKey(hashCode)) {
            createCounter(characters, ticksPerIncrement);
        }
        return iteratorMap.get(hashCode).getCurrentString();
    }

    private void createCounter(String characters, Integer ticksPerIncrement) {
        iteratorMap.put(getHashCode(characters, ticksPerIncrement), new Iterator(characters, ticksPerIncrement));
    }

    private final class Iterator {
        private final List<String> stringGroups = new ArrayList<>();
        private Integer currentIndex;
        private final Integer ticksPerIncrement;
        private Integer currentTicks;

        private Iterator(String characters, Integer ticksPerIncrement) {
            int initIndex = 0;
            char[] chars = characters.toCharArray();
            while (initIndex < characters.length()) {
                if (chars[initIndex] == '[') {
                    int groupIndex = initIndex + 1;
                    StringBuilder sb = new StringBuilder();
                    while (chars[groupIndex] != ']' && groupIndex < characters.length()) {
                        sb.append(chars[groupIndex]);
                        groupIndex++;
                    }
                    initIndex = groupIndex;
                    this.stringGroups.add(sb.toString());
                } else {
                    this.stringGroups.add(String.valueOf(chars[initIndex]));
                }
                initIndex++;
            }

            this.currentIndex = 0;
            this.ticksPerIncrement = ticksPerIncrement;
            this.currentTicks = 0;
        }

        public String getCurrentString() {
            return stringGroups.isEmpty() ? "" : stringGroups.get(currentIndex);
        }

        public List<String> getStringGroups() {
            return Collections.unmodifiableList(stringGroups);
        }

        public Integer getCurrentIndex() {
            return currentIndex;
        }

        public Integer getTicksPerIncrement() {
            return ticksPerIncrement;
        }

        public Integer getCurrentTicks() {
            return currentTicks;
        }

        public void setCurrentIndex(Integer currentIndex) {
            this.currentIndex = currentIndex;
        }

        public void setCurrentTicks(Integer currentTicks) {
            this.currentTicks = currentTicks;
        }
    }
}
