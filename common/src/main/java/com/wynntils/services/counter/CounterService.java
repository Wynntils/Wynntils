package com.wynntils.services.counter;

import com.wynntils.core.components.Service;
import com.wynntils.mc.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CounterService extends Service {
    private final Map<String, Counter> counterMap = new HashMap<>();

    public CounterService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        counterMap.forEach((id, counter) -> {
            if (counter.getCurrentTicks() >= counter.getMaxTicks()) {
                if (counter.getCurrent() >= counter.getMax()) {
                    counter.setCurrent(0);
                } else {
                    counter.setCurrent(counter.getCurrent() + 1);
                }
                counter.setCurrentTicks(0);
            } else {
                counter.setCurrentTicks(counter.getCurrentTicks() + 1);
            }
        });
    }

    public Integer getCounterValue(String id) {
        return counterMap.get(id).getCurrent();
    }

    public boolean counterExists(String id) {
        return counterMap.containsKey(id);
    }

    public void createCounter(Integer max, Integer maxTicks, String id) {
        counterMap.put(id, new Counter(max, maxTicks));
    }

    private final class Counter {
        private final Integer max;
        private Integer current;
        private final Integer maxTicks;
        private Integer currentTicks;

        private Counter(Integer max, Integer maxTicks) {
            this.max = max;
            this.current = 0;
            this.maxTicks = maxTicks;
            this.currentTicks = 0;
        }

        public Integer getMax() {
            return max;
        }

        public Integer getCurrent() {
            return current;
        }

        public Integer getMaxTicks() {
            return maxTicks;
        }

        public Integer getCurrentTicks() {
            return currentTicks;
        }

        public void setCurrent(Integer current) {
            this.current = current;
        }

        public void setCurrentTicks(Integer currentTicks) {
            this.currentTicks = currentTicks;
        }
    }

}
