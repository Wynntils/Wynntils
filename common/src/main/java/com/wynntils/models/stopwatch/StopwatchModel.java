/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stopwatch;

import com.wynntils.core.components.Model;
import java.util.List;

public class StopwatchModel extends Model {

    private long startTimeMillis = 0;
    private long pauseTimeMillis = 0;
    private boolean running = false;

    public StopwatchModel() {
        super(List.of());
    }

    public int getHours() {
        return (int) (getElapsedMillis() / 3600000) % 24;
    }

    public int getMinutes() {
        return (int) (getElapsedMillis() / 60000) % 60;
    }

    public int getSeconds() {
        return (int) (getElapsedMillis() / 1000) % 60;
    }

    public int getMilliseconds() {
        return (int) getElapsedMillis() % 1000;
    }

    private long getElapsedMillis() {
        return running ? System.currentTimeMillis() - startTimeMillis : pauseTimeMillis - startTimeMillis;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (running) return;

        if (pauseTimeMillis != 0) { // paused -> resumed
            startTimeMillis += System.currentTimeMillis() - pauseTimeMillis;
            pauseTimeMillis = 0;
        } else { // stopped -> started
            startTimeMillis = System.currentTimeMillis();
        }
        running = true;
    }

    public void pause() {
        if (!running) return;

        pauseTimeMillis = System.currentTimeMillis();
        running = false;
    }

    public void reset() {
        running = false;
        startTimeMillis = 0;
        pauseTimeMillis = 0;
    }
}
