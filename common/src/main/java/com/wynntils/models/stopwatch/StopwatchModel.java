package com.wynntils.models.stopwatch;

import com.wynntils.core.components.Model;

import java.util.List;

public class StopwatchModel extends Model {

    private long startTimeMillis = -1;
    private long currentTimeMillis = -1;
    private boolean running = false;

    public StopwatchModel() {
        super(List.of());
    }

    public int getHours() {
        update();
        return (int) ((currentTimeMillis - startTimeMillis) / 3600000) % 24;
    }

    public int getMinutes() {
        update();
        return (int) ((currentTimeMillis - startTimeMillis) / 60000) % 60;
    }

    public int getSeconds() {
        update();
        return (int) ((currentTimeMillis - startTimeMillis) / 1000) % 60;
    }

    public int getMilliseconds() {
        update();
        return (int) (currentTimeMillis - startTimeMillis) % 1000;
    }

    private void update() {
        if (running) {
            currentTimeMillis = System.currentTimeMillis();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (startTimeMillis == -1) {
            startTimeMillis = System.currentTimeMillis();
        }
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        running = false;
        startTimeMillis = -1;
        currentTimeMillis = -1;
    }
}
