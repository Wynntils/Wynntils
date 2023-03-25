package com.wynntils.models.stopwatch;

import com.wynntils.core.components.Model;

import java.util.List;

public class StopwatchModel extends Model {

    private long startTimeMillis = -1;
    private long pausedAtMillis = -1;
    private boolean running = false;

    public StopwatchModel() {
        super(List.of());
    }

    public int getHours() {
        return (int) ((running ? System.currentTimeMillis() : pausedAtMillis - startTimeMillis) / 3600000) % 24;
    }

    public int getMinutes() {
        return (int) ((running ? System.currentTimeMillis() : pausedAtMillis - startTimeMillis) / 60000) % 60;
    }

    public int getSeconds() {
        return (int) ((running ? System.currentTimeMillis() : pausedAtMillis - startTimeMillis) / 1000) % 60;
    }

    public int getMilliseconds() {
        return (int) (running ? System.currentTimeMillis() : pausedAtMillis - startTimeMillis) % 1000;
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
        pausedAtMillis = System.currentTimeMillis();
        running = false;
    }

    public void reset() {
        running = false;
        startTimeMillis = -1;
        pausedAtMillis = -1;
    }
}
