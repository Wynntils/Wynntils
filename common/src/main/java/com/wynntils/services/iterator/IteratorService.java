/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.iterator;

import com.wynntils.core.components.Service;
import com.wynntils.mc.event.TickEvent;
import java.util.ArrayList;
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
                if (iterator.getCurrentIndex() >= iterator.getCharacters().length - 1) {
                    toRemove.add(id);
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
        return iteratorMap.get(hashCode).getCurrentCharacter();
    }

    private void createCounter(String characters, Integer ticksPerIncrement) {
        iteratorMap.put(getHashCode(characters, ticksPerIncrement), new Iterator(characters, ticksPerIncrement));
    }

    private final class Iterator {
        private final char[] characters;
        private Integer currentIndex;
        private final Integer ticksPerIncrement;
        private Integer currentTicks;

        private Iterator(String characters, Integer ticksPerIncrement) {
            this.characters = characters.toCharArray();
            this.currentIndex = 0;
            this.ticksPerIncrement = ticksPerIncrement;
            this.currentTicks = 0;
        }

        public String getCurrentCharacter() {
            return characters.length == 0 ? "" : String.valueOf(characters[currentIndex]);
        }

        public char[] getCharacters() {
            return characters.clone();
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
