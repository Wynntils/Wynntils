/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.stopwatch;

import com.wynntils.core.components.Model;
import java.util.List;

public class StopwatchService extends Model {
    private long startTimeMillis = 0;
    private long elapsedBeforePause = 0;
    private boolean running = false;

    public StopwatchService() {
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

    public boolean isZero() {
        return getHours() == 0 && getMinutes() == 0 && getSeconds() == 0 && getMilliseconds() == 0;
    }

    private long getElapsedMillis() {
        return running ? System.currentTimeMillis() - startTimeMillis : elapsedBeforePause;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        if (running) return;

        if (elapsedBeforePause != 0) { // paused -> resumed
            startTimeMillis = System.currentTimeMillis() - elapsedBeforePause;
            elapsedBeforePause = 0;
        } else { // stopped -> started
            startTimeMillis = System.currentTimeMillis();
        }
        running = true;
    }

    public void pause() {
        if (!running) return;

        elapsedBeforePause = System.currentTimeMillis() - startTimeMillis;
        running = false;
    }

    public void reset() {
        running = false;
        startTimeMillis = 0;
        elapsedBeforePause = 0;
    }
}
