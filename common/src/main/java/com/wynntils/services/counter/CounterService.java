package com.wynntils.services.counter;

import com.wynntils.core.components.Service;
import com.wynntils.mc.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CounterService extends Service {
    private final Map<Integer, Counter> counterMap = new HashMap<>();

    public CounterService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        List<Integer> toRemove = new ArrayList<>();
        counterMap.forEach((id, counter) -> {
            if (counter.getCurrentTicks() >= counter.getTicksPerIncrement()) {
                if (counter.getCurrentValue() >= counter.getResetAfter()) {
                    toRemove.add(getHashCode(counter.getResetAfter(), counter.getTicksPerIncrement()));
                } else {
                    counter.setCurrentValue(counter.getCurrentValue() + 1);
                }
                counter.setCurrentTicks(0);
            } else {
                counter.setCurrentTicks(counter.getCurrentTicks() + 1);
            }
        });
        toRemove.forEach(counterMap::remove);
        toRemove.clear();
    }

    private int getHashCode(Integer resetAfter, Integer ticksPerIncrement) {
        return (resetAfter + "" + ticksPerIncrement).hashCode();
    }

    public Integer getCounterValue(Integer resetAfter, Integer ticksPerIncrement) {
        int hashCode = getHashCode(resetAfter, ticksPerIncrement);
        if (!counterMap.containsKey(hashCode)) {
            createCounter(resetAfter, ticksPerIncrement);
        }
        return counterMap.get(hashCode).getCurrentValue();
    }

    private void createCounter(Integer resetAfter, Integer ticksPerIncrement) {
        counterMap.put(getHashCode(resetAfter, ticksPerIncrement), new Counter(resetAfter, ticksPerIncrement));
    }

    private final class Counter {
        private final Integer resetAfter;
        private Integer currentValue;
        private final Integer ticksPerIncrement;
        private Integer currentTicks;

        private Counter(Integer resetAfter, Integer ticksPerIncrement) {
            this.resetAfter = resetAfter;
            this.currentValue = 0;
            this.ticksPerIncrement = ticksPerIncrement;
            this.currentTicks = 0;
        }

        public Integer getResetAfter() {
            return resetAfter;
        }

        public Integer getCurrentValue() {
            return currentValue;
        }

        public Integer getTicksPerIncrement() {
            return ticksPerIncrement;
        }

        public Integer getCurrentTicks() {
            return currentTicks;
        }

        public void setCurrentValue(Integer currentValue) {
            this.currentValue = currentValue;
        }

        public void setCurrentTicks(Integer currentTicks) {
            this.currentTicks = currentTicks;
        }
    }

}
